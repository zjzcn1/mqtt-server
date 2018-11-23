package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.store.PubRelMessage;
import com.github.zjzcn.mqtt.store.SessionStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_LEAST_ONCE;

public class PubRecHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PubRelHandler.class);

    private SessionStore sessionStore;

    public PubRecHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void handle(Channel channel, MqttMessage msg) {
        String clientId = NettyUtils.clientId(channel);
        int packetId = MessageUtils.packetId(msg);
        sessionStore.removeOutboundMessage(clientId, packetId);
        PubRelMessage pubRel = new PubRelMessage(clientId, packetId);
        sessionStore.storeSecondPhaseMessage(clientId, packetId, pubRel);
        log.debug("Stored PUBREL message into SecondPhaseMessageStore, clientId={}, packetId={}.", clientId, packetId);

        MqttFixedHeader pubRelHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, AT_LEAST_ONCE, false, 0);
        MqttMessage pubRelMessage = new MqttMessage(pubRelHeader, from(packetId));
        channel.writeAndFlush(pubRelMessage).addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

}
