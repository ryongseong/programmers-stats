package view;

import support.SvgTemplateLoader;

public class NicknameRenderer {

    private static final NicknameRenderer nicknameRenderer = new NicknameRenderer();

    private final SvgTemplateLoader TEMPLATE_LOADER = SvgTemplateLoader.getTemplateLoader();

    private static final int MAX_KOREAN_LENGTH = 4;
    private static final int MAX_NON_KOREAN_LENGTH = 8;
    private static final double FONT_SIZE_BASE = 1.35;

    private NicknameRenderer() {}

    public static NicknameRenderer getNicknameRenderer() {
        return nicknameRenderer;
    }

    private double calculateFontScale(int length, boolean hasKorean) {
        double scale;

        if (length > 14) {
            scale = 0.67;
        } else if (length > 10) {
            scale = 0.78;
        } else if (length > 6) {
            scale = 0.89;
        } else {
            scale = 1.0;
        }

        return hasKorean ? scale * 0.85 : scale;
    }

    private boolean isMultilineRequired(int length, boolean hasKorean) {
        return (hasKorean && length > MAX_KOREAN_LENGTH) ||
            (!hasKorean && length > MAX_NON_KOREAN_LENGTH);
    }

    private String renderSinglelineNickname(String nickname, String fontSize, int length) {
        String template = TEMPLATE_LOADER.loadTemplate("nickname_singleline.svg");

        return template
            .replace("{{y}}", "48")
            .replace("{{fontSize}}", fontSize)
            .replace("{{nickname}}", nickname);
    }

    private String renderMultilineNickname(String nickname, String fontSize, boolean hasKorean) {
        int splitIdx = hasKorean ? MAX_KOREAN_LENGTH : MAX_NON_KOREAN_LENGTH;
        String firstLine = nickname.substring(0, splitIdx);
        String secondLine = nickname.substring(splitIdx);
        String template = TEMPLATE_LOADER.loadTemplate("nickname_multiline.svg");

        return template
            .replace("{{fontSize}}", fontSize)
            .replace("{{firstLine}}", firstLine)
            .replace("{{secondLine}}", secondLine);
    }

    public String renderNickname(String nickname) {
        boolean hasKorean = nickname.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
        int nameLength = nickname.length();

        double fontScale = calculateFontScale(nameLength, hasKorean);
        String fontSize = String.format("%.2frem", FONT_SIZE_BASE * fontScale);

        if (isMultilineRequired(nameLength, hasKorean)) {
            return renderMultilineNickname(nickname, fontSize, hasKorean);
        } else {
            return renderSinglelineNickname(nickname, fontSize, nameLength);
        }
    }

}
