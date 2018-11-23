package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.ChannelManager;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.store.Session;
import com.github.zjzcn.mqtt.store.SessionStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;


public class DisconnectHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(DisconnectHandler.class);

    private SessionStore sessionStore;
    private ChannelManager channelManager;


    public DisconnectHandler(SessionStore sessionStore, ChannelManager channelManager) {
        this.sessionStore = sessionStore;
        this.channelManager = channelManager;
    }

    @Override
    public void handle(Channel channel, MqttMessage msg) {
        String clientId = NettyUtils.clientId(channel);

        Session session = sessionStore.getSession(clientId);
        if (session != null && session.isCleanSession()) {
            sessionStore.cleanSession(clientId);
            log.info("Cleaned session from SessionStore, clientId={}, session={}.", clientId, session);
        }

        channelManager.removeChannel(clientId);
        channel.close().addListener(CLOSE_ON_FAILURE);
        log.info("Client disconnected, clientId={}, channelId={}.", clientId, channel.id());
    }

}
