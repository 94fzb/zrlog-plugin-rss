package com.zrlog.plugin.rss.handle;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.api.IConnectHandler;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.type.RunType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectHandler implements IConnectHandler {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private AutoRefreshFeedFileRunnable autoRefreshFeedFileRunnable;

    @Override
    public void handler(IOSession ioSession, MsgPacket msgPacket) {
        this.autoRefreshFeedFileRunnable = new AutoRefreshFeedFileRunnable(ioSession);
        executorService.scheduleAtFixedRate(autoRefreshFeedFileRunnable, 0, 2, RunConstants.runType == RunType.BLOG ? TimeUnit.MINUTES : TimeUnit.HOURS);
    }

    public AutoRefreshFeedFileRunnable getAutoRefreshFeedFile() {
        return autoRefreshFeedFileRunnable;
    }
}