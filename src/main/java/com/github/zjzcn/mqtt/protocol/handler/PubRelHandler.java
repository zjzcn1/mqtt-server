package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessagePublisher;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.store.RetainedMessage;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.store.StoredMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

public class PubRelHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PubRelHandler.class);

    private MessagePublisher messagePublisher;
    private SessionStore sessionStore;


    public PubRelHandler(SessionStore sessionStore, MessagePublisher messagePublisher) {
        this.sessionStore = sessionStore;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public void handle(Channel channel, MqttMessage msg) {
        String clientId = NettyUtils.clientId(channel);
        int packetId = MessageUtils.packetId(msg);
        StoredMessage stored = sessionStore.removeInboundMessage(clientId, packetId);
        if (stored == null) {
            log.warn("Can't find message from InboundMessageStore, clientId={}, packetId={}.", clientId, packetId);
            throw new IllegalArgumentException("Can't find inbound message.");
        } else {
            log.debug("Removed PUBLISH message from InboundMessageStore, clientId={}, message={}.", clientId, stored);
        }

        String topic = stored.getTopic();
        MqttQoS mqttQoS = MqttQoS.valueOf(stored.getQos());
        ByteBuf payload = NettyUtils.bytes2byteBuf(stored.getPayload());
        messagePublisher.publishMessageToSubscribers(stored.getTopic(), mqttQoS, payload);

        if (stored.isRetained()) {
            if (stored.getPayload().length == 0) {
                RetainedMessage retained = sessionStore.removeRetainedMessage(topic);
                if (retained != null) {
                    log.debug("Removed PUBLISH message from RetainedMessageStore, clientId={}, packetId={}.", clientId, packetId);
                }
            } else {
                RetainedMessage retained = MessageUtils.asRetainedMessage(stored);
                sessionStore.storeRetainedMessage(topic, retained);
                log.debug("Stored PUBLISH message into RetainedMessageStore, clientId={}, packetId={}.", clientId, packetId);
            }
        }

        sendPubComp(channel, packetId);
    }

    private void sendPubComp(Channel channel, int packetId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, AT_MOST_ONCE, false, 0);
        MqttMessage pubCompMessage = new MqttMessage(fixedHeader, from(packetId));
        channel.writeAndFlush(pubCompMessage);
    }
}
