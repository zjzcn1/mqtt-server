package com.github.zjzcn.mqtt.protocol;


import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    private static final Logger log = LoggerFactory.getLogger(ChannelManager.class);

    // <clientId, Channel>
    private Map<String, Channel> channels = new ConcurrentHashMap<>();

    public void putChannel(String clientId, Channel channel) {
        channels.put(clientId, channel);
    }

    public Channel getChannel(String clientId) {
        return channels.get(clientId);
    }

    public Channel removeChannel(String clientId) {
        return channels.remove(clientId);
    }

    public boolean isActive(String clientId) {
        Channel channel = getChannel(clientId);
        if (channel != null && channel.isActive()) {
            return true;
        }
        return false;
    }

}
