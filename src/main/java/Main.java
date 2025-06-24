import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final String PROGRAMMERS_SIGN_IN = "https://programmers.co.kr/api/v1/account/sign-in";
    private static final String PROGRAMMERS_USER_RECORD = "https://programmers.co.kr/api/v1/users/record";

    public static void main(String[] args) {
        try {
            // 환경 변수를 가져옴
            String id = System.getenv("PROGRAMMERS_TOKEN_ID");
            String pw = System.getenv("PROGRAMMERS_TOKEN_PW");

            if (id == null || pw == null) {
                System.out.println("환경 변수 PROGRAMMERS_TOKEN_ID 와 PROGRAMMERS_TOKEN_PW를 설정해주세요.");
                return;
            }

            HttpClient client = HttpClient.newHttpClient();

            // 프로그래머스 로그인
            String signInPayload = String.format("{\"user\": {\"email\": \"%s\", \"password\": \"%s\"}}", id, pw);
            HttpRequest signInRequest = HttpRequest.newBuilder()
                    .uri(new URI(PROGRAMMERS_SIGN_IN))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(signInPayload))
                    .build();

            HttpResponse<String> signInResponse = client.send(signInRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println(signInResponse);
            if (signInResponse.statusCode() != 200) {
                System.err.println("로그인 실패: " + signInResponse.body());
                return;
            }

            // 모든 쿠키를 추출
            List<String> cookies = signInResponse.headers().allValues("set-cookie").stream()
                    .map(cookie -> cookie.split(";", 2)[0])  // 쿠키 값만 추출
                    .collect(Collectors.toList());
            String cookiesHeader = String.join("; ", cookies);

            // 사용자 정보 요청
            HttpRequest userRecordRequest = HttpRequest.newBuilder()
                    .uri(new URI(PROGRAMMERS_USER_RECORD))
                    .header("Cookie", cookiesHeader)
                    .GET()
                    .build();

            HttpResponse<String> userRecordResponse = client.send(userRecordRequest, HttpResponse.BodyHandlers.ofString());

            if (userRecordResponse.statusCode() != 200) {
                System.err.println("----");
                System.err.println(userRecordResponse);
                System.err.println("사용자 정보 요청 실패: " + userRecordResponse.body());
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> myData = objectMapper.readValue(userRecordResponse.body(), Map.class);

            // svg 뱃지 생성
            if (myData != null) {
                // 데이터를 안전하게 가져오기 위해 변수에 저장
                Object level = ((Map<String, Object>) myData.get("skillCheck")).get("level");
                Object score = ((Map<String, Object>) myData.get("ranking")).get("score");
                Object solved = ((Map<String, Object>) myData.get("codingTest")).get("solved");
                Object rank = ((Map<String, Object>) myData.get("ranking")).get("rank");

                // 데이터를 String 타입으로 변환하여 안전하게 사용
                String svgContent = String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                                "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"400px\" height=\"120px\" style=\"shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                                "<style>" +
                                ".title { fill: #222b3a; font-size: 0.85rem; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".desc { fill: #0078ff; font-size: 1.5rem; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".desc-2 { fill: #6b7684; font-size: 0.85rem; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".card-bg { fill: #e9eef6; stroke: #dbe3ec; stroke-width: 1; rx: 12; }" +
                                "</style>" +
                                // 카드 1개 배경
                                "<rect class=\"card-bg\" x=\"5\" y=\"5\" width=\"390\" height=\"110\" rx=\"12\"/>" +
                                // Programmers 로고 이미지와 텍스트(좌상단)
                                "<image x=\"20\" y=\"18\" width=\"24\" height=\"24\" xlink:href=\"programmers_logo.png\"/>" +
                                "<text x=\"52\" y=\"35\" font-size=\"1.1rem\" font-weight=\"bold\" fill=\"#222b3a\" font-family=\"-apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji\">Programmers</text>" +
                                // 정보 텍스트 묶음 (좌우 정렬)
                                // 정복 중인 레벨
                                "<text x=\"30\" y=\"70\" class=\"title\">정복 중인 레벨</text>" +
                                "<text x=\"140\" y=\"70\" class=\"desc\" text-anchor=\"end\">%s</text>" +
                                "<text x=\"155\" y=\"70\" class=\"desc-2\">레벨</text>" +
                                // 현재 점수
                                "<text x=\"180\" y=\"70\" class=\"title\">현재 점수</text>" +
                                "<text x=\"270\" y=\"70\" class=\"desc\" text-anchor=\"end\">%s</text>" +
                                "<text x=\"285\" y=\"70\" class=\"desc-2\">점</text>" +
                                // 해결한 코딩 테스트
                                "<text x=\"30\" y=\"100\" class=\"title\">해결한 코딩 테스트</text>" +
                                "<text x=\"140\" y=\"100\" class=\"desc\" text-anchor=\"end\">%s</text>" +
                                "<text x=\"155\" y=\"100\" class=\"desc-2\">문제</text>" +
                                // 나의 랭킹
                                "<text x=\"180\" y=\"100\" class=\"title\">나의 랭킹</text>" +
                                "<text x=\"270\" y=\"100\" class=\"desc\" text-anchor=\"end\">%s</text>" +
                                "<text x=\"285\" y=\"100\" class=\"desc-2\">위</text>" +
                                "</svg>",
                        level != null ? level.toString() : "",
                        score != null ? score.toString() : "",
                        solved != null ? solved.toString() : "",
                        rank != null ? rank.toString() : ""
                );

                Path currentPath = Paths.get("").toAbsolutePath();
                System.out.println("현재 작업 디렉토리: " + currentPath);

                Path fileDirectory = Paths.get("./result");
                if (!Files.exists(fileDirectory)) {
                    Files.createDirectories(fileDirectory);  // dist 디렉토리가 없으면 생성
                }

                Path templateFile = fileDirectory.resolve("result.svg");
                if (!Files.exists(templateFile)) {
                    Files.writeString(templateFile, "");  // result 파일이 없으면 빈 파일 생성
                }

                Path resultFile = fileDirectory.resolve("result.svg");

                // svg 뱃지 파일 생성
                Files.writeString(resultFile, svgContent);
                System.out.println("뱃지 생성 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}