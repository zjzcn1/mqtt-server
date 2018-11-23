package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.store.Subscription;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;
import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

public class UnsubscribeHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(UnsubscribeHandler.class);

    private SessionStore sessionStore;

    public UnsubscribeHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void handle(Channel channel, MqttMessage m) {
        MqttUnsubscribeMessage msg = (MqttUnsubscribeMessage) m;
        List<String> topics = msg.payload().topics();
        String clientId = NettyUtils.clientId(channel);
        int packetId = msg.variableHeader().messageId();
        for (String topicFilter : topics) {
            if (!MessageUtils.isValidTopic(topicFilter)) {
                // close the connection, not valid topicFilter is a protocol violation
                channel.close().addListener(CLOSE_ON_FAILURE);
                log.error("Topic filter is invalid. clientId={}, invalidTopicFilter={}", clientId, topicFilter);
                return;
            }

            Subscription sub = sessionStore.removeSubscription(clientId, topicFilter);
            if (sub != null) {
                log.info("Removed subscription from SubscriptionStore, clientId={}, topic={}.", clientId, topicFilter);
            }
        }

        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, AT_MOST_ONCE,
                false, 0);
        MqttUnsubAckMessage ackMessage = new MqttUnsubAckMessage(fixedHeader, from(packetId));

        channel.writeAndFlush(ackMessage).addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

}
