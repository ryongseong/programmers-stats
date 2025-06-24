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

                // 닉네임 변수 예시 (실제 데이터에 맞게 수정 필요)
                String nickname = myData.getOrDefault("nickname", "gyudol").toString();
                String svgContent = String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                                "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"370px\" height=\"210px\" style=\"shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                                "<defs>" +
                                "<linearGradient id=\"cardGradient\" x1=\"0%%\" y1=\"0%%\" x2=\"100%%\" y2=\"0%%\">" +
                                "<stop offset=\"0%%\" style=\"stop-color:#a8d8f8;stop-opacity:1\" />" +
                                "<stop offset=\"100%%\" style=\"stop-color:#3a5fc8;stop-opacity:1\" />" +
                                "</linearGradient>" +
                                "</defs>" +
                                "<style>" +
                                ".card-bg { stroke: #dbe3ec; stroke-width: 1; rx: 16; }" +
                                ".header { font-size: 1.35rem; font-weight: bold; fill: #ffffff; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; alignment-baseline: middle; }" +
                                ".nickname { font-size: 1.15rem; font-weight: bold; fill: #ffffff; font-family: inherit; margin-left: 10px; animation: twinkling 2.5s ease-in-out infinite; }" +
                                ".info-line { font-size: 1.08rem; fill: #ffffff; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; }" +
                                ".info-label { font-size: 1.08rem; fill: #ffffff; font-family: inherit; font-weight: 500; }" +
                                ".desc { fill: #ffffff; font-size: 1.18rem; font-weight: bold; font-family: inherit; animation: twinkling 2.5s ease-in-out infinite; opacity: 0.95; }" +
                                ".desc-2 { fill: #ffffff; font-size: 1.05rem; font-weight: bold; font-family: inherit; opacity: 0.7; }" +
                                "@keyframes twinkling { 0%% { opacity: 1; } 40%% { opacity: 1; } 50%% { opacity: 0.4; } 60%% { opacity: 1; } 100%% { opacity: 1; } }" +
                                "</style>" +
                                // 카드 배경
                                "<rect class=\"card-bg\" x=\"5\" y=\"5\" width=\"360\" height=\"200\" rx=\"16\" fill=\"url(#cardGradient)\"/>" +
                                // 좌측 logo 이미지 (크게, Programmers 아래)
                                "<image x=\"30\" y=\"60\" width=\"60\" height=\"60\" xlink:href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAMAAABF0y+mAAAAOVBMVEVHcEwgKz4gKz0gKz0VIzc3QE9LU2F3fYddZG+MkZkACiebn6Xg4eMAABgHGjH////P0NSvsrf19vfZo52CAAAAA3RSTlMASNfaYUakAAAAu0lEQVR4AX3TURKDMAhFURUCkSBG97/YJrZOYZr6fs/cwY84TfMCwy1zM/i7eYpdbCdwwxQ1YKI0RgJEIhoiMefUMA3LlXnlK0URjIjcsBSmJFmVNxEzvNE69uWyt6nWo/AbTdJ6HLWqnvtlBHaXuO5xiN+bmM5gKv6DLKbFPIJkdQgQEFCo1I8dErGzbeouOnSHq8EA5ewZC3h0Yc0bgkcXciCHxjuZl4A1WkDkaAEB4WfPT/PxUT/9Di8WvxDIKmTCowAAAABJRU5ErkJggg==\"/>" +
                                // 상단 텍스트(좌측, 닉네임 오른쪽)
                                "<text x=\"110\" y=\"45\" class=\"header\" text-anchor=\"start\">Programmers</text>" +
                                "<text x=\"240\" y=\"45\" class=\"nickname\" text-anchor=\"start\">%s</text>" +
                                // 정보 텍스트(라벨과 값 간격 넓힘, 가운데 정렬, 아래쪽 패딩)
                                "<g>" +
                                "<text x=\"90\" y=\"110\" class=\"info-label\" text-anchor=\"end\">Level</text>" +
                                "<text x=\"120\" y=\"110\" class=\"desc\" text-anchor=\"start\">%s</text>" +
                                "<text x=\"170\" y=\"110\" class=\"desc-2\" text-anchor=\"start\">level</text>" +
                                "<text x=\"90\" y=\"140\" class=\"info-label\" text-anchor=\"end\">Rate</text>" +
                                "<text x=\"120\" y=\"140\" class=\"desc\" text-anchor=\"start\">%s</text>" +
                                "<text x=\"170\" y=\"140\" class=\"desc-2\" text-anchor=\"start\">pt</text>" +
                                "<text x=\"90\" y=\"170\" class=\"info-label\" text-anchor=\"end\">Solved</text>" +
                                "<text x=\"120\" y=\"170\" class=\"desc\" text-anchor=\"start\">%s</text>" +
                                "<text x=\"170\" y=\"170\" class=\"desc-2\" text-anchor=\"start\">problems</text>" +
                                "<text x=\"90\" y=\"200\" class=\"info-label\" text-anchor=\"end\">Rank</text>" +
                                "<text x=\"120\" y=\"200\" class=\"desc\" text-anchor=\"start\">%s</text>" +
                                "<text x=\"170\" y=\"200\" class=\"desc-2\" text-anchor=\"start\">th</text>" +
                                "</g>" +
                                "</svg>",
                        nickname,
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