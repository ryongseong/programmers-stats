package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ProgrammersLoginDto;
import dto.ProgrammersUserInfoDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import properties.EnvProperties;

public class ProgrammersApiClient {

    private static final ProgrammersApiClient API_CLIENT = new ProgrammersApiClient();

    private static final String SIGN_IN = "https://programmers.co.kr/api/v1/account/sign-in";
    private static final String PROGRAMMERS_USER_RECORD = "https://programmers.co.kr/api/v1/users/record";

    private final EnvProperties ENV = EnvProperties.getEnvProperties();
    private final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ProgrammersApiClient() {}

    public static ProgrammersApiClient getApiClient() {
        return API_CLIENT;
    }

    public ProgrammersUserInfoDto fetchUserData() {
        try {
            // 1. 로그인 → 쿠키 추출
            List<String> cookies = login();
            if (cookies == null) return null;

            // 2. 사용자 정보 조회
            Map<String, Object> userData = fetchUserInfo(cookies);
            if (userData == null) return null;

            // 3. DTO 매핑
            return mapToDto(userData);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> login() throws Exception {
        String payload = OBJECT_MAPPER.writeValueAsString(new ProgrammersLoginDto(
            ENV.getId(), ENV.getPassword()));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(SIGN_IN))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("로그인 실패: " + response.body());
            return null;
        }

        return response.headers()
            .allValues("set-cookie").stream()
            .map(cookie -> cookie.split(";", 2)[0])
            .collect(Collectors.toList());
    }

    private Map<String, Object> fetchUserInfo(List<String> cookies) throws Exception {
        String cookieHeader = String.join("; ", cookies);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(PROGRAMMERS_USER_RECORD))
            .header("Cookie", cookieHeader)
            .GET()
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("사용자 정보 요청 실패: " + response.body());
            return null;
        }

        return OBJECT_MAPPER.readValue(response.body(), Map.class);
    }

    private ProgrammersUserInfoDto mapToDto(Map<String, Object> data) {
        try {
            String level = ((Map<String, Object>) data.get("skillCheck")).get("level").toString();
            String score = ((Map<String, Object>) data.get("ranking")).get("score").toString();
            String solved = ((Map<String, Object>) data.get("codingTest")).get("solved").toString();
            String rank = ((Map<String, Object>) data.get("ranking")).get("rank").toString();
            String nickname = data.getOrDefault("name", "Gyuchan Kim").toString();

            return ProgrammersUserInfoDto.builder()
                .nickname(nickname)
                .level(Integer.parseInt(level))
                .score(Integer.parseInt(score))
                .solved(Integer.parseInt(solved))
                .rank(Integer.parseInt(rank))
                .build();

        } catch (Exception e) {
            System.err.println("DTO 매핑 실패: " + e.getMessage());
            return null;
        }
    }
}