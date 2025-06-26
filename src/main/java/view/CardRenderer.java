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

    private final int GOLD_LEVEL_THRESHOLD = 5;
    private final int GOLD_SCORE_THRESHOLD = 1600;
    private final int GOLD_SOLVED_THRESHOLD = 333;
    private final int GOLD_RANK_THRESHOLD = 1000;

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

        if (level >= GOLD_LEVEL_THRESHOLD && score >= GOLD_SCORE_THRESHOLD &&
            solved >= GOLD_SOLVED_THRESHOLD && rank <= GOLD_RANK_THRESHOLD) {

            String diamond = TEMPLATE_LOADER.loadTemplate("level5_diamond.svg");
            return diamond == null ? "" : diamond;
        }

        return "";
    }

    private String getHighlightClass(boolean isGold) {
        return isGold ? "desc-gold" : "desc";
    }

    private String formatNumber(long num) {
        return NumberFormat.getInstance(Locale.US).format(num);
    }

    public String renderBadge(ProgrammersUserInfoDto userDto, String template) {
        if (userDto == null || template == null) return null;

        int level = userDto.getLevel(), score = userDto.getScore(),
            solved = userDto.getSolved(), rank = userDto.getRank();

        String nicknameSvg = NICKNAME_RENDERER.renderNickname(userDto.getNickname());
        String rankSuffix = RankFormatter.getOrdinalSuffix(rank);

        return template
            .replace("{{gradient}}", getGradientByLevel(level))
            .replace("{{levelBars}}", loadLevelBarsTemplate(level))
            .replace("{{shield}}", loadShieldTemplate(level))
            .replace("{{diamond}}", loadDiamondTemplate(userDto))
            .replace("{{nicknameSvg}}", nicknameSvg)
            .replace("{{levelClass}}", getHighlightClass(level >= GOLD_LEVEL_THRESHOLD))
            .replace("{{scoreClass}}", getHighlightClass(score >= GOLD_SCORE_THRESHOLD))
            .replace("{{solvedClass}}", getHighlightClass(solved >= GOLD_SOLVED_THRESHOLD))
            .replace("{{rankClass}}", getHighlightClass(rank <= GOLD_RANK_THRESHOLD))
            .replace("{{nicknameClass}}", getHighlightClass(rank <= GOLD_RANK_THRESHOLD))
            .replace("{{level}}", formatNumber(level))
            .replace("{{score}}", formatNumber(score))
            .replace("{{solved}}", formatNumber(solved))
            .replace("{{rank}}", formatNumber(rank))
            .replace("{{rankSuffix}}", rankSuffix);
    }
} 