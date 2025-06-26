package view;

import support.SvgTemplateLoader;

public class NicknameRenderer {

    private static final NicknameRenderer nicknameRenderer = new NicknameRenderer();

    private static final double BASE_FONT_SIZE = 1.35;
    private static final double KOREAN_SCALE_FACTOR = 0.8;

    private NicknameRenderer() {}

    public static NicknameRenderer getNicknameRenderer() {
        return nicknameRenderer;
    }

    private double calculateScale(int length, boolean hasKorean) {
        double scale;

        if (length > 18) {
            scale = 0.45;
        } else if (length > 14) {
            scale = 0.55;
        } else if (length > 10) {
            scale = 0.67;
        } else if (length > 8) {
            scale = 0.78;
        } else if (length > 6) {
            scale = 0.89;
        } else {
            scale = 1.0;
        }

        return hasKorean ? scale * KOREAN_SCALE_FACTOR : scale;
    }

    private String renderMultiline(String nickname, boolean hasKorean) {
        int spaceIdx = nickname.indexOf(" ");
        String firstLine = nickname.substring(0, spaceIdx);
        String secondLine = nickname.substring(spaceIdx + 1);
        
        // 각 라인의 길이에 비례해서 폰트 크기 계산
        double firstLineScale = calculateScale(firstLine.length(), hasKorean);
        double secondLineScale = calculateScale(secondLine.length(), hasKorean);
        
        String firstLineFontSize = String.format("%.2frem", BASE_FONT_SIZE * firstLineScale);
        String secondLineFontSize = String.format("%.2frem", BASE_FONT_SIZE * secondLineScale);

        return SvgTemplateLoader.loadTemplate("nickname_multiline.svg")
            .replace("{{firstLineFontSize}}", firstLineFontSize)
            .replace("{{secondLineFontSize}}", secondLineFontSize)
            .replace("{{firstLine}}", firstLine)
            .replace("{{secondLine}}", secondLine);
    }

    private String renderSingleline(String nickname, boolean hasKorean) {
        double scale = calculateScale(nickname.length(), hasKorean);
        String fontSize = String.format("%.2frem", BASE_FONT_SIZE * scale);

        return SvgTemplateLoader.loadTemplate("nickname_singleline.svg")
            .replace("{{fontSize}}", fontSize)
            .replace("{{nickname}}", nickname);
    }

    private boolean isMultilineRequired(String nickname) {
        return nickname.contains(" ");
    }

    public String renderNickname(String nickname) {
        boolean hasKorean = nickname.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");

        if (isMultilineRequired(nickname)) {
            return renderMultiline(nickname, hasKorean);
        }

        return renderSingleline(nickname, hasKorean);
    }

}
