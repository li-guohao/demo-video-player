package run.ikaros.demo.demovideoplayer.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PageController {

    @PostMapping("/page/video")
    public String toVideoPage(String url, String transcode, Model model){
        String retUrl = url;
        if("true".equalsIgnoreCase(transcode)) {
            // retUrl = "/transcode/video?url=" +
            //     new String(Base64.getEncoder()
            //         .encode(url.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        }
        model.addAttribute("url", retUrl);
        return "video.html";
    }

    @GetMapping("/video")
    public String toVideoPage(String url) {
        return "video.html";
    }
}
