package view;

import dto.ProgrammersUserInfoDto;
import java.text.NumberFormat;
import java.util.Locale;
import support.RankFormatter;
import support.SvgTemplateLoader;

public class CardRenderer {

    private static final CardRenderer CARD_RENDERER = new CardRenderer();

    private final SvgTemplateLoader TEMPLATE_LOADER = SvgTemplateLoader.getTemplateLoader();
    private final NicknameRenderer NICKNAME_RENDERER = NicknameRenderer.getNicknameRenderer();

    private CardRenderer() {}

    public static CardRenderer getCardRenderer() {
        return CARD_RENDERER;
    }

    private String getGradientByLevel(int level) {
        String fileName = "gradation_level" + level + ".svg";
        String gradation = TEMPLATE_LOADER.loadTemplate(fileName);

        if (gradation == null) {
            String fallback = TEMPLATE_LOADER.loadTemplate("gradation_default.svg");
            return fallback == null ? "" : fallback;
        }

        return gradation;
    }

    private String loadLevelBarsTemplate(int level) {
        String fileName = "level" + level + "_bars.svg";
        String bars = TEMPLATE_LOADER.loadTemplate(fileName);

        return bars == null ? "" : bars;
    }

    private String loadShieldTemplate(int level) {
        if (level == 4) {
            String shield = TEMPLATE_LOADER.loadTemplate("level4_shield.svg");
            return shield == null ? "" : shield;
        } else if (level == 5) {
            String shield = TEMPLATE_LOADER.loadTemplate("level5_shield.svg");
            return shield == null ? "" : shield;
        }

        return "";
    }

    private String loadDiamondTemplate(ProgrammersUserInfoDto userDto) {
        int level = userDto.getLevel(), score = userDto.getScore(),
            solved = userDto.getSolved(), rank = userDto.getRank();

        if (level == 5 && score >= 1600 && solved >= 333 && rank <= 1000) {
            String diamond = TEMPLATE_LOADER.loadTemplate("level5_diamond.svg");
            return diamond == null ? "" : diamond;
        }

        return "";
    }

    private String formatNumber(long num) {
        return NumberFormat.getInstance(Locale.US).format(num);
    }

    public String renderBadge(ProgrammersUserInfoDto userDto, String template) {
        if (userDto == null || template == null) return null;

        int level = userDto.getLevel();
        String nicknameSvg = NICKNAME_RENDERER.renderNickname(userDto.getNickname());
        String rankSuffix = RankFormatter.getOrdinalSuffix(userDto.getRank());

        return template
            .replace("{{gradient}}", getGradientByLevel(level))
            .replace("{{levelBars}}", loadLevelBarsTemplate(level))
            .replace("{{shield}}", loadShieldTemplate(level))
            .replace("{{diamond}}", loadDiamondTemplate(userDto))
            .replace("{{nicknameSvg}}", nicknameSvg)
            .replace("{{level}}", formatNumber(userDto.getLevel()))
            .replace("{{score}}", formatNumber(userDto.getScore()))
            .replace("{{solved}}", formatNumber(userDto.getSolved()))
            .replace("{{rank}}", formatNumber(userDto.getRank()))
            .replace("{{rankSuffix}}", rankSuffix);
    }
} 