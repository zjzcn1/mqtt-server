package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.store.StoredMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubAckHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PubAckHandler.class);

    private SessionStore sessionStore;

    public PubAckHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void handle(Channel channel, MqttMessage msg) {
        String clientId = NettyUtils.clientId(channel);
        int packetId = MessageUtils.packetId(msg);
        StoredMessage storedMessage = sessionStore.removeOutboundMessage(clientId, packetId);
        if (storedMessage != null) {
            log.debug("Removed PUBLISH message from OutboundMessageStore, clientId={}, storedMessage={}.", clientId, storedMessage);
        }
    }

}
