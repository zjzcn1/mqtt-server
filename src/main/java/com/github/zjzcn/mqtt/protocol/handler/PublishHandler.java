package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessagePublisher;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.store.RetainedMessage;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.store.StoredMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_LEAST_ONCE;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

public class PublishHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PublishHandler.class);

    private SessionStore sessionStore;
    private MessagePublisher messagePublisher;
    private Authenticator authenticator;

    public PublishHandler(SessionStore sessionStore,
                          Authenticator authenticator,
                          MessagePublisher messagePublisher) {
        this.sessionStore = sessionStore;
        this.messagePublisher = messagePublisher;
        this.authenticator = authenticator;
    }

    @Override
    public void handle(Channel channel, MqttMessage m) {
        MqttPublishMessage msg = (MqttPublishMessage) m;
        String clientId = NettyUtils.clientId(channel);
        String username = NettyUtils.username(channel);
        String topic = msg.variableHeader().topicName();
        MqttQoS qos = msg.fixedHeader().qosLevel();
        int packetId = msg.variableHeader().packetId();

        if (!authenticator.canWrite(topic, username)) {
            log.warn("MQTT client have not write permission, clientId={}, topic={}, username={}.", clientId, topic, username);
            return;
        }

        switch (qos) {
            case AT_MOST_ONCE:
                // route message to subscribers
                messagePublisher.publishMessageToSubscribers(topic,qos, msg.payload());
                break;
            case AT_LEAST_ONCE:
                // route message to subscribers
                messagePublisher.publishMessageToSubscribers(topic,qos, msg.payload());
                sendPubAck(channel, packetId);
                break;
            case EXACTLY_ONCE:
                StoredMessage storedMsg = MessageUtils.asStoredMessage(msg);
                sessionStore.storeInboundMessage(clientId, packetId, storedMsg);
                log.debug("Stored PUBLISH message into InboundMessageStore, clientId={}, topic={}, packetId={}.", clientId, topic, packetId);
                sendPubRec(channel, packetId);
                break;
            default:
                log.error("Unknown QoS Type: {}", qos);
                break;
        }

        if (qos == AT_MOST_ONCE || qos == AT_LEAST_ONCE) {
            if (msg.fixedHeader().isRetain()) {
                if (msg.payload().isReadable()) {
                    RetainedMessage retained = MessageUtils.asRetainedMessage(msg);
                    sessionStore.storeRetainedMessage(topic, retained);
                    log.debug("Stored PUBLISH message into RetainedMessageStore, clientId={}, packetId={}.", clientId, packetId);
                } else {
                    RetainedMessage retained = sessionStore.removeRetainedMessage(topic);
                    if (retained != null) {
                        log.debug("Removed PUBLISH message from RetainedMessageStore, clientId={}, packetId={}.", clientId, packetId);
                    }
                }
            }
        }
    }

    private void sendPubAck(Channel channel, int packetId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, AT_MOST_ONCE, false, 0);
        MqttPubAckMessage pubAckMessage = new MqttPubAckMessage(fixedHeader, from(packetId));
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRec(Channel channel, int packetId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, AT_MOST_ONCE, false, 0);
        MqttMessage pubRecMessage = new MqttMessage(fixedHeader, from(packetId));
        channel.writeAndFlush(pubRecMessage);
    }

}
