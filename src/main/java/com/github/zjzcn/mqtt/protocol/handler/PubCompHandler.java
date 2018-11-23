package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.store.SessionStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubCompHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PubCompHandler.class);

    private SessionStore sessionStore;

    public PubCompHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void handle(Channel channel, MqttMessage msg) {
        String clientId = NettyUtils.clientId(channel);
        int packetId = MessageUtils.packetId(msg);
        sessionStore.removeSecondPhaseMessage(clientId, packetId);
        log.debug("Removed PUBREL message from SecondPhaseMessageStore, clientId={}, packetId={}.", clientId, packetId);
    }
}
