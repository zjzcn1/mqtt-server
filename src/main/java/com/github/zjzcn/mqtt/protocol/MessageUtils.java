package com.github.zjzcn.mqtt.protocol;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.store.RetainedMessage;
import com.github.zjzcn.mqtt.store.StoredMessage;
import com.github.zjzcn.mqtt.store.WillMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

public class MessageUtils {

    public static boolean isMatchTopic(String topic, String topicFilter) {
        String[] splitTopics = StringUtils.split(topic, '/');
        String[] splitTopicFilters = StringUtils.split(topicFilter, '/');
        if (splitTopics.length < splitTopicFilters.length) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        if (topic.startsWith("/")) {
            sb.append("/");
        }
        for (int i = 0; i < splitTopicFilters.length; i++) {
            String value = splitTopicFilters[i];
            if (value.equals("+")) {
                sb.append("+/");
            } else if (value.equals("#")) {
                sb.append("#/");
                break;
            } else {
                sb.append(splitTopics[i]).append("/");
            }
        }
        String newTopicFilter = StringUtils.removeEnd(sb.toString(), "/");

        return topicFilter.equals(newTopicFilter);
    }

    public static boolean isValidTopic(String topic) {
        if (topic.length() == 0) {
            return false;
        }
        String[] splits = topic.split("/");
        for (int i = 0; i < splits.length; i++) {
            String s = splits[i];
            if (s.equals("#")) {
                // check that multi is the last symbol
                if (i != splits.length - 1) {
                    return false;
                }
            } else if (s.contains("#")) {
                return false;
            } else if (!s.equals("+") && s.contains("+")) {
                return false;
            }
        }

        return true;
    }

    public static StoredMessage asStoredMessage(MqttPublishMessage msg) {
        ByteBuf payload = msg.payload();
        byte[] payloadBytes = NettyUtils.byteBuf2bytes(payload);

        StoredMessage stored = new StoredMessage();
        stored.setPayload(payloadBytes);
        stored.setQos(msg.fixedHeader().qosLevel().value());
        stored.setTopic(msg.variableHeader().topicName());
        stored.setRetained(msg.fixedHeader().isRetain());
        return stored;
    }

    public static RetainedMessage asRetainedMessage(MqttPublishMessage msg) {
        ByteBuf payload = msg.payload();
        byte[] payloadBytes = NettyUtils.byteBuf2bytes(payload);
        RetainedMessage retailed = new RetainedMessage();
        retailed.setPayload(payloadBytes);
        retailed.setQos(msg.fixedHeader().qosLevel().value());
        retailed.setTopic(msg.variableHeader().topicName());
        return retailed;
    }

    public static RetainedMessage asRetainedMessage(StoredMessage msg) {
        RetainedMessage retailed = new RetainedMessage();
        retailed.setPayload(msg.getPayload());
        retailed.setQos(msg.getQos());
        retailed.setTopic(msg.getTopic());
        return retailed;
    }

    public static WillMessage asWillMessage(MqttConnectMessage msg) {
        WillMessage will = new WillMessage();
        will.setWillPayload(msg.payload().willMessageInBytes());
        will.setWillQos(msg.variableHeader().willQos());
        will.setWillTopic(msg.payload().willTopic());
        will.setWillRetain(msg.variableHeader().isWillRetain());
        return will;
    }


    public static int packetId(MqttMessage msg) {
        if (msg.variableHeader() instanceof MqttPublishVariableHeader) {
            return ((MqttPublishVariableHeader) msg.variableHeader()).packetId();
        } else {
            return ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        }
    }

    public static MqttConnAckMessage createConnAck(MqttConnectReturnCode returnCode) {
        return createConnAck(returnCode, false);
    }

    public static MqttConnAckMessage createConnAck(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE,
                false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    public static MqttPublishMessage createNotRetainedPublish(String topic, MqttQoS qos, ByteBuf payload) {
        return createNotRetainedPublish(topic, qos, payload, 0);
    }

    public static MqttPublishMessage createNotRetainedPublish(String topic, MqttQoS qos, ByteBuf payload, int packetId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
        MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, packetId);
        return new MqttPublishMessage(fixedHeader, varHeader, payload);
    }

    public static MqttSubAckMessage createSubAck(List<MqttTopicSubscription> topicFilters, int packetId) {
        List<Integer> grantedQoSLevels = new ArrayList<>();
        for (MqttTopicSubscription req : topicFilters) {
            grantedQoSLevels.add(req.qualityOfService().value());
        }

        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, AT_MOST_ONCE, false, 0);
        MqttSubAckPayload payload = new MqttSubAckPayload(grantedQoSLevels);
        return new MqttSubAckMessage(fixedHeader, from(packetId), payload);
    }

}
