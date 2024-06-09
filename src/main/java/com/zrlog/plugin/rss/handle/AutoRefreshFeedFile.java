package com.zrlog.plugin.rss.handle;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.IOUtil;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.BlogRunTime;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.rss.controller.RssController;
import com.zrlog.plugin.rss.service.FeedService;
import com.zrlog.plugin.type.ActionType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class AutoRefreshFeedFile implements Runnable {

    public static final String DEFAULT_URI_PATH = "/rss.xml";

    private static final Logger LOGGER = LoggerUtil.getLogger(RssController.class);

    private final IOSession ioSession;

    public AutoRefreshFeedFile(IOSession ioSession) {
        this.ioSession = ioSession;
    }

    @Override
    public void run() {
        String path = ioSession.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.BLOG_RUN_TIME, BlogRunTime.class).getPath();
        String content = new FeedService(ioSession).feed();
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "uriPath");
        ioSession.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map responseMap = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            String uriPath = Objects.requireNonNullElse((String) responseMap.get("uriPath"), DEFAULT_URI_PATH);
            File rssFile = new File(path + uriPath);
            rssFile.getParentFile().mkdirs();
            IOUtil.writeBytesToFile(content.getBytes(), rssFile);
            try {
                Map<String, String[]> map = new HashMap<>();
                map.put("fileInfo", new String[]{rssFile + ",/" + rssFile.getName() + ",true"});
                ioSession.requestService("uploadService", map);
            } catch (Exception e) {
                LOGGER.warning("upload to service failed " + e.getMessage());
            }
        });

    }
}
