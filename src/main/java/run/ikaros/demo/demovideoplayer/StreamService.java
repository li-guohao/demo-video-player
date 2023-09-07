package run.ikaros.demo.demovideoplayer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

import static run.ikaros.demo.demovideoplayer.StreamCons.MEDIA_INFO_MAP;

@Slf4j
@Service
public class StreamService {

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public StreamService(
        @Qualifier("executor") ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public String start(String url) {
        String md5 = Md5Utils.digest(url);
        MediaThread mediaThread = MEDIA_INFO_MAP.get(md5);
        if (mediaThread != null) {
            return mediaThread.getUrl();
        }

        log.info("will start transcoding for url: {}", url);
        // start media thread
        mediaThread = new MediaThread(md5, url);
        MEDIA_INFO_MAP.put(md5, mediaThread);
        threadPoolTaskExecutor.execute(mediaThread);

        Path transcodeM3u8Path = ClassPathStaticUtils.getTranscodeM3u8Path(md5);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (Files.notExists(transcodeM3u8Path)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return mediaThread.getUrl();
    }

    public void stop(String url) {
        log.info("will stop transcoding for url: {}", url);
        String md5 = Md5Utils.digest(url);
        MediaThread mediaThread = MEDIA_INFO_MAP.get(md5);
        if(mediaThread != null) {
            mediaThread.release();
        }
    }
}
