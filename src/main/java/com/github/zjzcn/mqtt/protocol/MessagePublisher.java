package com.github.zjzcn.mqtt.protocol;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.store.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;

public class MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);

    private ChannelManager channelManager;
    private SessionStore sessionStore;

    public MessagePublisher(SessionStore sessionStore, ChannelManager channelManager) {
        this.sessionStore = sessionStore;
        this.channelManager = channelManager;
    }

    public void publishMessageToSubscribers(String topic, MqttQoS mqttQoS, ByteBuf payload) {
        Collection<Subscription> subs = sessionStore.searchSubscriptions(topic);
        log.debug("Search topic subscriptions, topic={}, subscriptions={}.", topic, subs);
        for (Subscription sub : subs) {
            String clientId = sub.getClientId();
            int lowestQoS = Math.min(mqttQoS.value(), sub.getQos());
            MqttQoS publishQos = MqttQoS.valueOf(lowestQoS);
            publishMessage(clientId, topic, publishQos, payload);
        }
    }

    public void publishWillMessageToSubscribers(String willClientId) {
        Session session = sessionStore.getSession(willClientId);
        if (session != null && session.getWillMessage() != null) {
            WillMessage willMessage = session.getWillMessage();
            log.debug("Get WillMessage, clientId={}, willMessage={}.", willClientId, willMessage);
            String topic = willMessage.getWillTopic();
            MqttQoS mqttQoS = MqttQoS.valueOf(willMessage.getWillQos());
            ByteBuf payload = NettyUtils.bytes2byteBuf(willMessage.getWillPayload());
            publishMessageToSubscribers(topic, mqttQoS, payload);
        }
    }

    public void publishRetainedMessages(Subscription newSubscription) {
        String clientId = newSubscription.getClientId();
        String topicFilter = newSubscription.getTopicFilter();
        Collection<RetainedMessage> messages = sessionStore.searchRetainedMessages(newSubscription.getTopicFilter());
        log.debug("Search retainedMessages for new subscription, clientId={}, topicFilter={}, messages={}.",
                clientId, topicFilter, messages);
        for (RetainedMessage retailed : messages) {
            String topic = retailed.getTopic();
            MqttQoS mqttQoS = MqttQoS.valueOf(retailed.getQos());
            ByteBuf payload = NettyUtils.bytes2byteBuf(retailed.getPayload());
            publishMessage(clientId, topic, mqttQoS, payload);
        }
    }

    public void republishStoredMessage(String clientId) {
        Map<Integer, StoredMessage> outboundMessages = sessionStore.getOutboundMessages(clientId);
        log.debug("Get storedOutboundMessages, clientId={}, messages={}.", clientId, outboundMessages);
        Map<Integer, StoredMessage> sortedMessages = new TreeMap<>(outboundMessages);
        for (Integer packetId : sortedMessages.keySet()) {
            StoredMessage storedMessage = sortedMessages.get(packetId);
            String topic = storedMessage.getTopic();
            MqttQoS mqttQoS = MqttQoS.valueOf(storedMessage.getQos());
            ByteBuf payload = NettyUtils.bytes2byteBuf(storedMessage.getPayload());
            publishMessage(clientId, topic, mqttQoS, payload, packetId);
        }

        Map<Integer, PubRelMessage> secondPhaseMessages = sessionStore.getSecondPhaseMessages(clientId);
        log.debug("Get storedPubRelMessages, clientId={}, messages={}.", clientId, secondPhaseMessages);
        Map<Integer, PubRelMessage> pubRelMessages = new TreeMap<>(secondPhaseMessages);
        for (Integer packetId : pubRelMessages.keySet()) {
            sendPubRelMessage(clientId, packetId);
        }
    }

    private void publishMessage(String clientId, String topic, MqttQoS mqttQoS, ByteBuf payload) {
        publishMessage(clientId, topic, mqttQoS, payload, null);
    }

    private void publishMessage(String clientId, String topic, MqttQoS mqttQoS, ByteBuf origPayload, Integer packetId) {
        ByteBuf payload = origPayload.retainedDuplicate();
        MqttPublishMessage publishMsg;
        if (mqttQoS == MqttQoS.AT_MOST_ONCE) {
            publishMsg = MessageUtils.createNotRetainedPublish(topic, mqttQoS, payload);
        } else {
            // QoS 1 or 2
            if (packetId == null || packetId <= 0) {
                packetId = sessionStore.nextPacketId(clientId);
            }
            publishMsg = MessageUtils.createNotRetainedPublish(topic, mqttQoS, payload, packetId);
            StoredMessage stored = MessageUtils.asStoredMessage(publishMsg);
            // store message
            sessionStore.storeOutboundMessage(clientId, packetId, stored);
        }

        sendMessage(clientId, publishMsg);
    }

    private void sendPubRelMessage(String clientId, int packetId) {
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                from(packetId), null);
        sendMessage(clientId, pubRelMessage);
    }

    private void sendMessage(String clientId, MqttMessage msg) {
        MqttMessageType messageType = msg.fixedHeader().messageType();
        if (channelManager.isActive(clientId)) {
            try {
                Channel channel = channelManager.getChannel(clientId);
                channel.writeAndFlush(msg);
            } catch (Throwable e) {
                log.error("Unable to send {} message, clientId={}.", messageType, clientId, e);
            }
        } else {
            log.warn("Channel is inactive, unable to send {} message, clientId={}.", messageType, clientId);
        }
    }
}
