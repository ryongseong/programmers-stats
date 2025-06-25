package support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SvgFileWriter {

    private static final SvgFileWriter FILE_WRITER = new SvgFileWriter();

    private SvgFileWriter() {}

    public static SvgFileWriter getFileWriter() {
        return FILE_WRITER;
    }

    public void writeToFile(String svgContent, String filePath) {
        try {
            Files.writeString(Path.of(filePath), svgContent);
        } catch (IOException e) {
            System.err.println("SVG 파일 저장 실패: " + e.getMessage());
        }
    }
} 