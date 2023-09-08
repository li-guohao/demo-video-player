package run.ikaros.demo.demovideoplayer;

import io.micrometer.common.util.StringUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static run.ikaros.demo.demovideoplayer.StreamCons.MEDIA_INFO_MAP;

@Slf4j
@Getter
public class MediaThread extends Thread {

    private final String key;
    private final String originalUrl;
    @Setter
    private String url;
    private boolean running = false;
    private boolean grabberStatus = false;
    private boolean recorderStatus = false;
    protected FFmpegFrameGrabber grabber;
    protected FFmpegFrameRecorder recorder;
    private boolean startStatus = true;
    private String startMsg;


    public MediaThread(String key, String originalUrl) {
        this.key = key;
        this.originalUrl = originalUrl;
        this.url = "/transcode/" + key + ".m3u8";
    }

    @Override
    public void run() {
        this.createGrabber().createRecodeRecorder().transform();
    }

    public MediaThread createGrabber() {
        log.info("创建拉流器…… | {}", originalUrl);
        // 拉流器
        grabber = new FFmpegFrameGrabber(originalUrl);
        // 超时时间(5秒)
        grabber.setOption("stimoout", "5000000");
        grabber.setOption("threads", "1");
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        // 设置缓存大小，提高画质、减少卡顿花屏
        grabber.setOption("buffer_size", "2048000");
        try {
            grabber.start();
            grabberStatus = true;
        } catch (FrameGrabber.Exception e) {
            log.error("{} | 创建拉流器异常！", originalUrl, e);
            startStatus = false;
            startMsg = "创建拉流器异常: " + e.getMessage();
        }
        return this;
    }

    public void closeGrabber() {
        try {
            log.info("关闭拉流器…… | {}", originalUrl);
            if (Objects.nonNull(grabber)) {
                grabber.flush();
                grabber.close();
            }

        } catch (IOException e) {
            log.error("关闭拉流器异常 | {}", originalUrl, e);
        }
    }

    public MediaThread createRecodeRecorder() {
        if (!grabberStatus) {
            return this;
        }
        doCreateRecodeRecorder();
        return this;
    }


    private void doCreateRecodeRecorder() {
        Path filePath = ClassPathStaticUtils.getTranscodeM3u8Path(key);

        recorder =
            new FFmpegFrameRecorder(filePath.toString(), grabber.getImageWidth(),
                grabber.getImageHeight(),
                grabber.getAudioChannels());
        recorder.setFormat("hls");

        //关于hls_wrap的说明，hls_wrap表示重复覆盖之前ts切片，这是一个过时配置，ffmpeg官方推荐使用hls_list_size 和hls_flags delete_segments代替hls_wrap
        //设置单个ts切片的时间长度（以秒为单位）。默认值为2秒
        recorder.setOption("hls_time", "2");
        //不根据gop间隔进行切片,强制使用hls_time时间进行切割ts分片
        // recorder.setOption("hls_flags", "split_by_time");

        //设置播放列表条目的最大数量。如果设置为0，则列表文件将包含所有片段，默认值为5
        // 当切片的时间不受控制时，切片数量太小，就会有卡顿的现象
        // recorder.setOption("hls_list_size", "8");
        //自动删除切片，如果切片数量大于hls_list_size的数量，则会开始自动删除之前的ts切片，只保留hls_list_size个数量的切片
        // recorder.setOption("hls_flags", "delete_segments");
        //ts切片自动删除阈值，默认值为1，表示早于hls_list_size+1的切片将被删除
        // recorder.setOption("hls_delete_threshold", "1");
        /*hls的切片类型：
         * 'mpegts'：以MPEG-2传输流格式输出ts切片文件，可以与所有HLS版本兼容。
         * 'fmp4':以Fragmented MP4(简称：fmp4)格式输出切片文件，类似于MPEG-DASH，fmp4文件可用于HLS version 7和更高版本。
         */
        recorder.setOption("hls_segment_type", "mpegts");
        // 设置这个参数，会将文件生成到本地
        //指定ts切片生成名称规则，按数字序号生成切片,例如'file%03d.ts'，就会生成file000.ts，file001.ts，file002.ts等切片文件
        //  recorder.setOption("hls_segment_filename", path + "-%5d.ts");
        // 设置第一个切片的编号
        recorder.setOption("start_number", "1");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        // 转码
        log.info("启动Hls转码录制器…… | {}", originalUrl);
        //  设置零延迟
        recorder.setVideoOption("tune", "zerolatency");
        // 快速
        recorder.setVideoOption("preset", "ultrafast");
        // recorder.setVideoOption("crf", "26");
        // recorder.setVideoOption("threads", "1");
        recorder.setFrameRate(25);// 设置帧率
        recorder.setGopSize(25);// 设置gop,与帧率相同，相当于间隔1秒 一个关键帧
        recorder.setVideoBitrate(20 * 1000 * 1000);// 码率20mb/s
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        try {
            recorder.start();
            recorderStatus = true;
        } catch (FrameRecorder.Exception e) {
            log.error("创建转码录制器异常！ | {}", originalUrl, e);
            startStatus = false;
            startMsg = "创建转码录制器失败！" + e.getMessage();
        }

    }


    public void closeRecodeRecorder() {
        try {
            log.info("关闭推流器…… | {}", originalUrl);
            if (Objects.nonNull(recorder)) {
                recorder.flush();
                recorder.close();
            }

        } catch (IOException e) {
            log.error("关闭推流器异常 | {}", originalUrl, e);
        }
    }

    public void transform() {
        if (!grabberStatus || !recorderStatus) {
            log.warn("status");
            return;
        }
        log.info("开启转流操作…… | {}", originalUrl);
        try {
            grabber.flush();
        } catch (FrameGrabber.Exception e) {
            log.error("清空拉流器缓存失败！| {} ", originalUrl, e);
        }

        running = true;

        while (running) {
            try {
                Frame frame = grabber.grabFrame();
                if (frame == null) {
                    log.error("frame is null | {}", originalUrl);
                }
                recorder.record(frame);
            } catch (Exception e) {
                log.error("转码操作异常！ | {}", originalUrl, e);
            }
            // log.info("{} | 转流操作结束", originalUrl);
        }
    }

    public void stopTransform() {
        this.running = false;
    }

    public void release() {
        log.info("release media thread for url: {}", url);
        MEDIA_INFO_MAP.remove(key);
        stopTransform();
        closeGrabber();
        closeRecodeRecorder();
        for (File file : Objects.requireNonNull(ClassPathStaticUtils.getTranscodePath().toFile()
            .listFiles((dir, name) -> name.contains(key)))) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

}
