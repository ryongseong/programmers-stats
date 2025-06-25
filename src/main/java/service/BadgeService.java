package service;

import client.ProgrammersApiClient;
import dto.ProgrammersUserInfoDto;
import view.CardRenderer;
import support.SvgFileWriter;
import support.SvgTemplateLoader;

public class BadgeService {

    private static final BadgeService BADGE_SERVICE = new BadgeService();

    private final ProgrammersApiClient apiClient = ProgrammersApiClient.getApiClient();
    private final SvgTemplateLoader svgTemplateLoader = SvgTemplateLoader.getTemplateLoader();
    private final CardRenderer cardRenderer = CardRenderer.getCardRenderer();
    private final SvgFileWriter svgFileWriter = SvgFileWriter.getFileWriter();

    private final String OUTPUT_FILE_PATH = "output/result.svg";

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

        String template = svgTemplateLoader.loadTemplate("badge_base.svg");
        String svg = cardRenderer.renderBadge(userDto, template);

        svgFileWriter.writeToFile(svg, OUTPUT_FILE_PATH);
    }
} 