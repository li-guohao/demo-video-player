package run.ikaros.demo.demovideoplayer;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/stream/hls")
public class HlsTranscodeController {
    private final StreamService streamService;

    public HlsTranscodeController(StreamService streamService) {
        this.streamService = streamService;
    }

    @PutMapping("/start")
    public String startTranscoding(@RequestParam String url) {
        return streamService.start(url);
    }

    @PutMapping("/close")
    public void closeTranscoding(@RequestParam String url) {
        streamService.stop(url);
    }

    @GetMapping("/get/{address}/{name}")
    public void getHls(@PathVariable("address") String address,
                       @PathVariable("name") String name) {

    }

    @RequestMapping("/record/{address}/{name}")
    public void recordHls(HttpServletRequest request,
                          @PathVariable("address") String address,
                          @PathVariable("name") String name) {

    }


}
