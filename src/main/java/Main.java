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

            // svg 배지 생성
            if (myData != null) {
                // 데이터를 안전하게 가져오기 위해 변수에 저장
                Object level = ((Map<String, Object>) myData.get("skillCheck")).get("level");
                Object score = ((Map<String, Object>) myData.get("ranking")).get("score");
                Object solved = ((Map<String, Object>) myData.get("codingTest")).get("solved");
                Object rank = ((Map<String, Object>) myData.get("ranking")).get("rank");

                String nickname = myData.getOrDefault("name", "Gyuchan Kim").toString();
                nickname = "Gyuchan Kim";

                // 숫자 콤마 포맷 적용
                NumberFormat nf = NumberFormat.getInstance();
                int levelInt = 0;
                String levelStr = "";
                if (level != null) {
                    try {
                        // levelInt = Integer.parseInt(level.toString());
                        levelInt = 3;
                    } catch(Exception e) { levelInt = 0; }
                    levelStr = nf.format(levelInt);
                }
//                String scoreStr = score != null ? nf.format(Long.parseLong("1600")) : "";
//                String solvedStr = solved != null ? nf.format(Long.parseLong("333")) : "";
//                String rankStr = rank != null ? nf.format(Long.parseLong("1000")) : "";
                String scoreStr = score != null ? nf.format(Long.parseLong(score.toString())) : "";
                String solvedStr = solved != null ? nf.format(Long.parseLong(solved.toString())) : "";
                String rankStr = rank != null ? nf.format(Long.parseLong(rank.toString())) : "";

                // SVG 배지 생성
                String svgContent = generateBadgeSVG(nickname, levelInt, levelStr, scoreStr, solvedStr, rankStr);

                Path currentPath = Paths.get("").toAbsolutePath();
                System.out.println("현재 작업 디렉토리: " + currentPath);

                Path fileDirectory = Paths.get("./output");
                if (!Files.exists(fileDirectory)) {
                    Files.createDirectories(fileDirectory);  // dist 디렉토리가 없으면 생성
                }

                Path templateFile = fileDirectory.resolve("result.svg");
                if (!Files.exists(templateFile)) {
                    Files.writeString(templateFile, "");  // result 파일이 없으면 빈 파일 생성
                }

                Path resultFile = fileDirectory.resolve("result.svg");

                // svg 배지 파일 생성
                Files.writeString(resultFile, svgContent);
                System.out.println("배지 생성 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 레벨에 따른 그라데이션 색상을 반환합니다.
     * @param level 사용자 레벨
     * @return 그라데이션 색상 정보 (시작색, 끝색)
     */
    private static String[] getGradientColorsByLevel(int level) {
        switch (level) {
            case 1:
                return new String[]{"#FFD700", "#FFA500"}; // 노랑
            case 2:
                return new String[]{"#32CD32", "#228B22"}; // 초록
            case 3:
                return new String[]{"#1E90FF", "#1a3399"}; // 파랑
            case 4:
                return new String[]{"#9370DB", "#8A2BE2"}; // 보라
            case 5:
                return new String[]{"#2F2F2F", "#000000"}; // 검정
            default:
                return new String[]{"#CD7F32", "#B87333"}; // 브라운
        }
    }

    /**
     * 레벨 바 SVG를 생성합니다.
     * @param levelInt 사용자 레벨
     * @return 레벨 바 SVG 문자열
     */
    private static String generateLevelBars(int levelInt) {
        StringBuilder levelBars = new StringBuilder();
        int baseX = 300, baseY = 90, barGap = 32;
        if (levelInt == 1) {
            // 레벨 1: 바 1개
            int y = baseY;
            levelBars.append(String.format(
                "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y, y + 28, y
            ));
            levelBars.append(String.format(
                "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y, y + 22, y
            ));
        } else if (levelInt == 2) {
            // 레벨 2: 바 2개
            for (int i = 0; i < 2; i++) {
                int y = baseY + i * barGap;
                levelBars.append(String.format(
                    "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y, y + 28, y
                ));
                levelBars.append(String.format(
                    "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y, y + 22, y
                ));
            }
        } else if (levelInt == 3) {
            // 레벨 3: 뒤집힌 바(뒤) + 바 2개(앞)
            int y1 = baseY;
            int y2 = baseY + barGap;
            int y3 = y1 + 28; // 첫번째 바의 아래 꼭짓점
            // 1. 뒤집힌 바(뒤에)
            levelBars.append("<g>");
            levelBars.append(String.format(
                "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y3 + 28, y3, y3 + 28
            ));
            levelBars.append(String.format(
                "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y3 + 22, y3, y3 + 22
            ));
            levelBars.append("</g>");
            // 2. 일반 바 2개(앞에)
            for (int i = 0; i < 2; i++) {
                int y = baseY + i * barGap;
                levelBars.append(String.format(
                    "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y, y + 28, y
                ));
                levelBars.append(String.format(
                    "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y, y + 22, y
                ));
            }
        } else if (levelInt == 4) {
            // 레벨 4: 레벨 2 + 실제 방패 스타일(더 위로, glow 효과, 첫번째 막대 중간까지)
            for (int i = 0; i < 2; i++) {
                int y = baseY + i * barGap;
                levelBars.append(String.format(
                    "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y, y + 28, y
                ));
                levelBars.append(String.format(
                    "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y, y + 22, y
                ));
            }
            // 실제 방패 스타일(더 위로, glow 효과, 첫번째 막대 중간까지)
            levelBars.append("<polygon points='320,105 300,145 305,185 335,185 340,145' style='fill:#fff;stroke:#bfa76a;stroke-width:4;opacity:0.85;filter:url(#glow);' />"
                + "<polygon points='320,120 303,148 307,180 333,180 337,148' style='fill:#f8f8f8;stroke:none;opacity:0.7;' />");
        } else if (levelInt >= 5) {
            // 레벨 5: 뒤집힌 바(뒤) + 바 2개(앞) + 실제 방패 스타일(더 위로, glow 효과, 첫번째 막대 중간까지)
            int y1 = baseY;
            int y2 = baseY + barGap;
            int y3 = y1 + 28; // 첫번째 바의 아래 꼭짓점
            // 1. 뒤집힌 바(뒤에)
            levelBars.append("<g>");
            levelBars.append(String.format(
                "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y3 + 28, y3, y3 + 28
            ));
            levelBars.append(String.format(
                "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y3 + 22, y3, y3 + 22
            ));
            levelBars.append("</g>");
            // 2. 일반 바 2개(앞에)
            for (int i = 0; i < 2; i++) {
                int y = baseY + i * barGap;
                levelBars.append(String.format(
                    "<polyline class=\"level-v-glow\" points=\"290,%d 320,%d 350,%d\" />", y, y + 28, y
                ));
                levelBars.append(String.format(
                    "<polyline class=\"level-v\" points=\"295,%d 320,%d 345,%d\" />", y, y + 22, y
                ));
            }
            // 실제 방패 스타일(더 위로, glow 효과, 첫번째 막대 중간까지)
            levelBars.append("<polygon points='320,105 300,145 305,185 335,185 340,145' style='fill:#fff;stroke:#bfa76a;stroke-width:4;opacity:0.85;filter:url(#glow);' />"
                + "<polygon points='320,120 303,148 307,180 333,180 337,148' style='fill:#f8f8f8;stroke:none;opacity:0.7;' />");
        }
        return levelBars.toString();
    }

    // 닉네임이 영문 16자, 한글 8자를 초과하면 ... 말줄임표로 표시
    private static String trimNickname(String nickname) {
        boolean hasKorean = nickname.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
        int maxLen = hasKorean ? 8 : 16;
        if (nickname.length() > maxLen) {
            return nickname.substring(0, maxLen) + "...";
        }
        return nickname;
    }

    /**
     * 프로그래머스 배지 SVG를 생성합니다.
     * @param nickname 사용자 닉네임
     * @param levelInt 사용자 레벨 (정수)
     * @param levelStr 사용자 레벨 (문자열)
     * @param scoreStr 점수
     * @param solvedStr 해결한 문제 수
     * @param rankStr 순위
     * @return 완성된 SVG 문자열
     */
    private static String generateBadgeSVG(String nickname, int levelInt, String levelStr, 
                                         String scoreStr, String solvedStr, String rankStr) {
        String[] gradientColors = getGradientColorsByLevel(levelInt);
        String levelBars = generateLevelBars(levelInt);
        String displayNickname = trimNickname(nickname);

        // 한글 포함 여부
        boolean hasKorean = displayNickname.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
        int nickLen = displayNickname.length();
        // 닉네임 폰트 크기 동적 조정
        double fontScale = 1.0;
        if (nickLen > 6 && nickLen <= 10) fontScale = 0.89;
        else if (nickLen > 10 && nickLen <= 14) fontScale = 0.78;
        else if (nickLen > 14) fontScale = 0.67;
        if (hasKorean) fontScale *= 0.85;
        String nicknameFontSize = String.format("%.2frem", 1.35 * fontScale);

        // 닉네임 x좌표 및 정렬
        String nicknameAnchor = "start";
        int nicknameX = 280;
        if (nickLen <= 4) {
            nicknameAnchor = "middle";
            nicknameX = 320;
        }

        // 숫자별 금색 효과 조건
        boolean isGoldRank = false, isGoldLevel = false, isGoldScore = false, isGoldSolved = false, isGoldNickname = false;
        try {
            String rankNum = rankStr.replace(",", "").replaceAll("[^0-9]", "");
            if (!rankNum.isEmpty()) {
                int rankVal = Integer.parseInt(rankNum);
                if (rankVal <= 1000) {
                    isGoldRank = true;
                    isGoldNickname = true;
                }
            }
        } catch(Exception e) {}
        if (levelInt == 5) isGoldLevel = true;
        try {
            long scoreVal = Long.parseLong(scoreStr.replace(",", ""));
            if (scoreVal >= 1600) isGoldScore = true;
        } catch(Exception e) {}
        try {
            long solvedVal = Long.parseLong(solvedStr.replace(",", ""));
            if (solvedVal >= 333) isGoldSolved = true;
        } catch(Exception e) {}
        String levelStyle = isGoldLevel ? "fill:#FFD700;filter:url(#glow);" : "";
        String scoreStyle = isGoldScore ? "fill:#FFD700;filter:url(#glow);" : "";
        String solvedStyle = isGoldSolved ? "fill:#FFD700;filter:url(#glow);" : "";
        String rankStyle = isGoldRank ? "fill:#FFD700;filter:url(#glow);" : "";
        String nicknameStyle = isGoldNickname ? "fill:#FFD700;filter:url(#glow);" : "";
        String desc2Style = "";

        // 닉네임 줄바꿈 처리 (한글 4자 초과, 영문/숫자 8자 초과 시 두 줄, 두 줄 모두 가운데 정렬)
        String nicknameSvg;
        if ((hasKorean && nickLen > 4) || (!hasKorean && nickLen > 8)) {
            int splitIdx = hasKorean ? 4 : 8;
            String firstLine = displayNickname.substring(0, splitIdx);
            String secondLine = displayNickname.substring(splitIdx);
            nicknameSvg = String.format(
                "<text x=\"320\" y=\"42\" class=\"nickname\" text-anchor=\"middle\" style=\"font-size:%s;%s\">%s<tspan x=\"320\" dy=\"1.3em\">%s</tspan></text>",
                nicknameFontSize, nicknameStyle, firstLine, secondLine
            );
        } else {
            int x = (nickLen <= 4) ? 320 : nicknameX;
            String anchor = (nickLen <= 4) ? "middle" : nicknameAnchor;
            nicknameSvg = String.format(
                "<text x=\"%d\" y=\"48\" class=\"nickname\" text-anchor=\"%s\" style=\"font-size:%s;%s\">%s</text>",
                x, anchor, nicknameFontSize, nicknameStyle, displayNickname
            );
        }

        return String.format("""
            <?xml version=\"1.0\" encoding=\"UTF-8\"?>
            <!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">
            <svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"390px\" height=\"200px\"
                 style=\"shape-rendering:geometricPrecision; text-rendering:geometricPrecision;
                        image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd\"
                 xmlns:xlink=\"http://www.w3.org/1999/xlink\">
              <defs>
                <linearGradient id=\"cardGradient\" x1=\"0%%\" y1=\"0%%\" x2=\"100%%\" y2=\"0%%\">
                  <stop offset=\"0%%\" style=\"stop-color:%s;stop-opacity:1\" />
                  <stop offset=\"100%%\" style=\"stop-color:%s;stop-opacity:1\" />
                </linearGradient>
                <filter id=\"glow\" x=\"-50%%\" y=\"-50%%\" width=\"200%%\" height=\"200%%\">
                  <feDropShadow dx=\"0\" dy=\"0\" stdDeviation=\"3\" flood-color=\"#e0eaff\" flood-opacity=\"0.9\"/>
                </filter>
              </defs>
              <style>
                .card-bg { stroke: #dbe3ec; stroke-width: 1; rx: 14; }
                .header { font-size: 1.25rem; font-weight: bold; fill: #fff; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji; alignment-baseline: middle; }
                .logo-img { vertical-align: middle; }
                .nickname { font-weight: bold; fill: #fff; font-family: inherit; animation: twinkling 2.5s ease-in-out infinite; }
                .info-label { font-size: 1.02rem; fill: #fff; font-family: inherit; font-weight: 500; }
                .desc { fill: #fff; font-size: 1.12rem; font-weight: bold; font-family: inherit; animation: twinkling 2.5s ease-in-out infinite; opacity: 0.95; }
                .desc-2 { fill: #fff; font-size: 0.98rem; font-weight: bold; font-family: inherit; opacity: 0.7; }
                .level-v { fill: none; stroke: #fff; stroke-width: 8; stroke-linecap: round; opacity: 0.95; }
                .level-v-glow { fill: none; stroke: #e0eaff; stroke-width: 16; stroke-linecap: round; opacity: 0.55; filter: url(#glow); }
                @keyframes twinkling { 0%% { opacity: 1; } 40%% { opacity: 1; } 50%% { opacity: 0.4; } 60%% { opacity: 1; } 100%% { opacity: 1; } }
              </style>
              <rect class=\"card-bg\" x=\"5\" y=\"5\" width=\"380\" height=\"190\" rx=\"14\" fill=\"url(#cardGradient)\"/>
              <image x=\"30\" y=\"20\" width=\"28\" height=\"28\" class=\"logo-img\"
                     xlink:href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAMAAABF0y+mAAAAOVBMVEVHcEwgKz4gKz0gKz0VIzc3QE9LU2F3fYddZG+MkZkACiebn6Xg4eMAABgHGjH////P0NSvsrf19vfZo52CAAAAA3RSTlMASNfaYUakAAAAu0lEQVR4AX3TURKDMAhFURUCkSBG97/YJrZOYZr6fs/cwY84TfMCwy1zM/i7eYpdbCdwwxQ1YKI0RgJEIhoiMefUMA3LlXnlK0URjIjcsBSmJFmVNxEzvNE69uWyt6nWo/AbTdJ6HLWqnvtlBHaXuO5xiN+bmM5gKv6DLKbFPIJkdQgQEFCo1I8dErGzbeouOnSHq8EA5ewZC3h0Yc0bgkcXciCHxjuZl4A1WkDkaAEB4WfPT/PxUT/9Di8WvxDIKmTCowAAAABJRU5ErkJggg==\"/>
              <text x=\"65\" y=\"42\" class=\"header\" text-anchor=\"start\">Programmers</text>
              %s
              <g>%s</g>
              <g>
                <text x=\"90\" y=\"90\" class=\"info-label\" text-anchor=\"end\">Level</text>
                <text x=\"120\" y=\"90\" class=\"desc\" text-anchor=\"start\" style=\"%s\">%s</text>
                <text x=\"200\" y=\"90\" class=\"desc-2\" text-anchor=\"start\" style=\"%s\">level</text>
                <text x=\"90\" y=\"120\" class=\"info-label\" text-anchor=\"end\">Rate</text>
                <text x=\"120\" y=\"120\" class=\"desc\" text-anchor=\"start\" style=\"%s\">%s</text>
                <text x=\"200\" y=\"120\" class=\"desc-2\" text-anchor=\"start\" style=\"%s\">pt</text>
                <text x=\"90\" y=\"150\" class=\"info-label\" text-anchor=\"end\">Solved</text>
                <text x=\"120\" y=\"150\" class=\"desc\" text-anchor=\"start\" style=\"%s\">%s</text>
                <text x=\"200\" y=\"150\" class=\"desc-2\" text-anchor=\"start\" style=\"%s\">problems</text>
                <text x=\"90\" y=\"180\" class=\"info-label\" text-anchor=\"end\">Rank</text>
                <text x=\"120\" y=\"180\" class=\"desc\" text-anchor=\"start\" style=\"%s\">%s</text>
                <text x=\"200\" y=\"180\" class=\"desc-2\" text-anchor=\"start\" style=\"%s\">th</text>
              </g>
            </svg>
            """,
            gradientColors[0], gradientColors[1], // 그라데이션 색상
            nicknameSvg,
            levelBars,
            levelStyle, levelStr, desc2Style,
            scoreStyle, scoreStr, desc2Style,
            solvedStyle, solvedStr, desc2Style,
            rankStyle, rankStr, desc2Style
        );
    }
}