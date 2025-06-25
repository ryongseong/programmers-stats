package support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SvgTemplateLoader {

    private static final SvgTemplateLoader TEMPLATE_LOADER = new SvgTemplateLoader();

    private static final String TEMPLATE_DIR = "src/main/resources/templates/";

    private SvgTemplateLoader() {}

    public static SvgTemplateLoader getTemplateLoader() {
        return TEMPLATE_LOADER;
    }

    public String loadTemplate(String templateName) {
        try {
            return Files.readString(Paths.get(TEMPLATE_DIR + templateName));
        } catch (IOException e) {
            System.err.println("SVG 템플릿 로딩 실패: " + e.getMessage());
            return null;
        }
    }
} 