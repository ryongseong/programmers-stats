package support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SvgFileWriter {

    public static void writeToFile(String svgContent, String filePath) {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();

        try {
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.writeString(path, svgContent);
        } catch (IOException e) {
            System.err.println("SVG 파일 저장 실패: " + e.getMessage());
        }
    }

} 