package service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import client.ProgrammersApiClient;
import dto.ProgrammersUserInfoDto;
import dto.UserInfoTestDto;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import support.SvgFileWriter;
import support.SvgTemplateLoader;
import support.TestDataLoader;
import view.CardRenderer;

class BadgeServiceTest {

    private final ProgrammersApiClient apiClient = ProgrammersApiClient.getApiClient();
    private final CardRenderer cardRenderer = CardRenderer.getCardRenderer();

    private final String OUTPUT_FILE_PATH = "output/result.svg";

    @Test
    void testGenerateBadge() {

        UserInfoTestDto userInfo = TestDataLoader.loadTestData(
            "badge-test.yml", UserInfoTestDto.class);

        ProgrammersUserInfoDto userDto = ProgrammersUserInfoDto.builder()
            .nickname(userInfo.getNickname())
            .level(userInfo.getLevel())
            .score(userInfo.getScore())
            .solved(userInfo.getSolved())
            .rank(userInfo.getRank())
            .build();

        String template = SvgTemplateLoader.loadTemplate("badge_base.svg");
        String svg = cardRenderer.renderBadge(userDto, template);

        SvgFileWriter.writeToFile(svg, OUTPUT_FILE_PATH);
        assertTrue(Files.exists(Paths.get(OUTPUT_FILE_PATH)), "결과 SVG 파일이 생성되어야 합니다");
    }

} 