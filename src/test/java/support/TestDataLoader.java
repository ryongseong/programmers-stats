package support;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.InputStream;

public class TestDataLoader {

    public static <T> T loadTestData(String fileName, Class<T> clazz) {
        Yaml yaml = new Yaml(new Constructor(clazz, new LoaderOptions()));

        try (InputStream in = TestDataLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IllegalArgumentException("YAML 파일을 찾을 수 없습니다: " + fileName);
            }

            return yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("YAML 로드 중 오류 발생: " + fileName, e);
        }
    }

} 