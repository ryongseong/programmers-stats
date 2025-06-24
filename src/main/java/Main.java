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
import java.text.NumberFormat;

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
                String nickname = myData.getOrDefault("name", "gyudol").toString();

                // 숫자 콤마 포맷 적용
                NumberFormat nf = NumberFormat.getInstance();
                int levelInt = 1;
                String levelStr = "";
                if (level != null) {
                    try { levelInt = Integer.parseInt(level.toString()); } catch(Exception e) { levelInt = 1; }
                    levelStr = nf.format(levelInt);
                }
                String scoreStr = score != null ? nf.format(Long.parseLong(score.toString())) : "";
                String solvedStr = solved != null ? nf.format(Long.parseLong(solved.toString())) : "";
                String rankStr = rank != null ? nf.format(Long.parseLong(rank.toString())) : "";

                // 레벨 바(작대기) SVG 생성 (왼쪽 위로 이동)
                StringBuilder levelBars = new StringBuilder();
                int baseX = 300, baseY = 90, barGap = 32;
                for (int i = 0; i < levelInt; i++) {
                    int y = baseY + i * barGap;
                    // 바깥쪽(빛나는 효과)
                    levelBars.append(String.format(
                        "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y, y + 28, y
                    ));
                    // 안쪽(흰색)
                    levelBars.append(String.format(
                        "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y, y + 22, y
                    ));
                }

                String svgContent = String.format("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                    <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="390px" height="200px"
                         style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision;
                                image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
                         xmlns:xlink="http://www.w3.org/1999/xlink">
                      <defs>
                        <linearGradient id="cardGradient" x1="0%%" y1="0%%" x2="100%%" y2="0%%">
                          <stop offset="0%%" style="stop-color:#7dbbe6;stop-opacity:1" />
                          <stop offset="100%%" style="stop-color:#1a3399;stop-opacity:1" />
                        </linearGradient>
                        <filter id="glow" x="-50%%" y="-50%%" width="200%%" height="200%%">
                          <feDropShadow dx="0" dy="0" stdDeviation="3" flood-color="#e0eaff" flood-opacity="0.9"/>
                        </filter>
                      </defs>
                      <style>
                        .card-bg { stroke: #dbe3ec; stroke-width: 1; rx: 14; }
                        .header { font-size: 1.25rem; font-weight: bold; fill: #fff; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; alignment-baseline: middle; }
                        .logo-img { vertical-align: middle; }
                        .nickname { font-size: 1.35rem; font-weight: bold; fill: #fff; font-family: inherit; animation: twinkling 2.5s ease-in-out infinite; }
                        .info-label { font-size: 1.02rem; fill: #fff; font-family: inherit; font-weight: 500; }
                        .desc { fill: #fff; font-size: 1.12rem; font-weight: bold; font-family: inherit; animation: twinkling 2.5s ease-in-out infinite; opacity: 0.95; }
                        .desc-2 { fill: #fff; font-size: 0.98rem; font-weight: bold; font-family: inherit; opacity: 0.7; }
                        .level-v { fill: none; stroke: #fff; stroke-width: 8; stroke-linecap: round; opacity: 0.95; }
                        .level-v-glow { fill: none; stroke: #e0eaff; stroke-width: 16; stroke-linecap: round; opacity: 0.55; filter: url(#glow); }
                        @keyframes twinkling { 0%% { opacity: 1; } 40%% { opacity: 1; } 50%% { opacity: 0.4; } 60%% { opacity: 1; } 100%% { opacity: 1; } }
                      </style>
                      <rect class="card-bg" x="5" y="5" width="380" height="190" rx="14" fill="url(#cardGradient)"/>
                      <image x="30" y="20" width="28" height="28" class="logo-img"
                             xlink:href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAMAAABF0y+mAAAAOVBMVEVHcEwgKz4gKz0gKz0VIzc3QE9LU2F3fYddZG+MkZkACiebn6Xg4eMAABgHGjH////P0NSvsrf19vfZo52CAAAAA3RSTlMASNfaYUakAAAAu0lEQVR4AX3TURKDMAhFURUCkSBG97/YJrZOYZr6fs/cwY84TfMCwy1zM/i7eYpdbCdwwxQ1YKI0RgJEIhoiMefUMA3LlXnlK0URjIjcsBSmJFmVNxEzvNE69uWyt6nWo/AbTdJ6HLWqnvtlBHaXuO5xiN+bmM5gKv6DLKbFPIJkdQgQEFCo1I8dErGzbeouOnSHq8EA5ewZC3h0Yc0bgkcXciCHxjuZl4A1WkDkaAEB4WfPT/PxUT/9Di8WvxDIKmTCowAAAABJRU5ErkJggg=="/>
                      <text x="65" y="42" class="header" text-anchor="start">Programmers</text>
                      <text x="280" y="42" class="nickname" text-anchor="start">%s</text>
                      <g>%s</g>
                      <g>
                        <text x="90" y="90" class="info-label" text-anchor="end">Level</text>
                        <text x="140" y="90" class="desc" text-anchor="start">%s</text>
                        <text x="200" y="90" class="desc-2" text-anchor="start">level</text>
                        <text x="90" y="120" class="info-label" text-anchor="end">Rate</text>
                        <text x="140" y="120" class="desc" text-anchor="start">%s</text>
                        <text x="200" y="120" class="desc-2" text-anchor="start">pt</text>
                        <text x="90" y="150" class="info-label" text-anchor="end">Solved</text>
                        <text x="140" y="150" class="desc" text-anchor="start">%s</text>
                        <text x="200" y="150" class="desc-2" text-anchor="start">problems</text>
                        <text x="90" y="180" class="info-label" text-anchor="end">Rank</text>
                        <text x="140" y="180" class="desc" text-anchor="start">%s</text>
                        <text x="200" y="180" class="desc-2" text-anchor="start">th</text>
                      </g>
                    </svg>
                    """,
                    nickname,
                    levelBars.toString(),
                    levelStr,
                    scoreStr,
                    solvedStr,
                    rankStr
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