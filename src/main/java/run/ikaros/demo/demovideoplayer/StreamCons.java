package run.ikaros.demo.demovideoplayer;

import java.util.concurrent.ConcurrentHashMap;

public interface StreamCons {
    ConcurrentHashMap<String, MediaThread> MEDIA_INFO_MAP = new ConcurrentHashMap<>();
}
