package run.ikaros.demo.demovideoplayer;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassPathStaticUtils {
    public static Path getTranscodeM3u8Path(String key) {
        File staticFile;
        try {
            staticFile = ResourceUtils.getFile("classpath:static");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Path transcodePath = Path.of(staticFile.toURI()).resolve("transcode");
        if(Files.notExists(transcodePath)) {
            try {
                Files.createDirectory(transcodePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return transcodePath.resolve(key + ".m3u8");
    }
}
