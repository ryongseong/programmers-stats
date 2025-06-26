package support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SvgTemplateLoader {

    private static final String TEMPLATE_DIR = "src/main/resources/templates/";

    public static String loadTemplate(String templateName) {
        try {
            Path path = Paths.get(TEMPLATE_DIR + templateName);

            if (!Files.exists(path)) {
                throw new IOException("템플릿 파일이 존재하지 않습니다: " + templateName);
            }

            return Files.readString(path);
        } catch (IOException e) {
            System.err.println("SVG 템플릿 로딩 실패: " + e.getMessage());
            return null;
        }
    }
} 