package run.ikaros.demo.demovideoplayer;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassPathStaticUtils {

    public static Path getTranscodePath() {
        File staticFile;
        try {
            staticFile = ResourceUtils.getFile("classpath:static");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Path.of(staticFile.toURI()).resolve("transcode");
    }

    public static Path getTranscodeM3u8Path(String key) {
        Path transcodePath = getTranscodePath();
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
