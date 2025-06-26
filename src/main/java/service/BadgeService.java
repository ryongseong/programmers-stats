package service;

import client.ProgrammersApiClient;
import dto.ProgrammersUserInfoDto;
import view.CardRenderer;
import support.SvgFileWriter;
import support.SvgTemplateLoader;

public class BadgeService {

    private static final BadgeService BADGE_SERVICE = new BadgeService();

    private static final String OUTPUT_FILE_PATH = "output/result.svg";

    private final ProgrammersApiClient apiClient = ProgrammersApiClient.getApiClient();
    private final CardRenderer cardRenderer = CardRenderer.getCardRenderer();

    private BadgeService() {}

    public static BadgeService getBadgeService() {
        return BADGE_SERVICE;
    }

    public void generateBadge() {
        ProgrammersUserInfoDto userDto = apiClient.fetchUserData();

        if (userDto == null) {
            System.err.println("유저 데이터 조회 실패");
            return;
        }

        String template = SvgTemplateLoader.loadTemplate("badge_base.svg");
        String svg = cardRenderer.renderBadge(userDto, template);

        SvgFileWriter.writeToFile(svg, OUTPUT_FILE_PATH);
    }

}