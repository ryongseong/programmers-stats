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
                                "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"280px\" height=\"140px\" style=\"shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                                "<style>" +
                                ".title { fill: #222b3a; font-size: 0.85rem; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".desc { fill: #0078ff; font-size: 1.2rem; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".desc-2 { fill: #6b7684; font-size: 0.85rem; font-weight: bold; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".card-bg { fill: #e9eef6; stroke: #dbe3ec; stroke-width: 1; rx: 12; }" +
                                "</style>" +
                                // 카드 1개 배경
                                "<rect class=\"card-bg\" x=\"5\" y=\"5\" width=\"270\" height=\"130\" rx=\"12\"/>" +
                                // Programmers 로고(base64)와 텍스트(상단 중앙)
                                "<image x=\"30\" y=\"18\" width=\"24\" height=\"24\" xlink:href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAMAAABF0y+mAAAAOVBMVEVHcEwgKz4gKz0gKz0VIzc3QE9LU2F3fYddZG+MkZkACiebn6Xg4eMAABgHGjH////P0NSvsrf19vfZo52CAAAAA3RSTlMASNfaYUakAAAAu0lEQVR4AX3TURKDMAhFURUCkSBG97/YJrZOYZr6fs/cwY84TfMCwy1zM/i7eYpdbCdwwxQ1YKI0RgJEIhoiMefUMA3LlXnlK0URjIjcsBSmJFmVNxEzvNE69uWyt6nWo/AbTdJ6HLWqnvtlBHaXuO5xiN+bmM5gKv6DLKbFPIJkdQgQEFCo1I8dErGzbeouOnSHq8EA5ewZC3h0Yc0bgkcXciCHxjuZl4A1WkDkaAEB4WfPT/PxUT/9Di8WvxDIKmTCowAAAABJRU5ErkJggg==\"/>" +
                                "<text x=\"140\" y=\"38\" font-size=\"1.1rem\" font-weight=\"bold\" fill=\"#222b3a\" text-anchor=\"middle\" font-family=\"-apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji\">Programmers</text>" +
                                // 정보 텍스트 묶음 (각 줄마다 가운데 정렬)
                                "<text x=\"140\" y=\"70\" font-size=\"0.95rem\" fill=\"#222b3a\" text-anchor=\"middle\">정복 중인 레벨 <tspan class=\"desc\">%s</tspan><tspan class=\"desc-2\">레벨</tspan></text>" +
                                "<text x=\"140\" y=\"90\" font-size=\"0.95rem\" fill=\"#222b3a\" text-anchor=\"middle\">현재 점수 <tspan class=\"desc\">%s</tspan><tspan class=\"desc-2\">점</tspan></text>" +
                                "<text x=\"140\" y=\"110\" font-size=\"0.95rem\" fill=\"#222b3a\" text-anchor=\"middle\">해결한 코딩 테스트 <tspan class=\"desc\">%s</tspan><tspan class=\"desc-2\">문제</tspan></text>" +
                                "<text x=\"140\" y=\"130\" font-size=\"0.95rem\" fill=\"#222b3a\" text-anchor=\"middle\">나의 랭킹 <tspan class=\"desc\">%s</tspan><tspan class=\"desc-2\">위</tspan></text>" +
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