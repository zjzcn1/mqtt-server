package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessagePublisher;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.store.Subscription;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
import static io.netty.handler.codec.mqtt.MqttQoS.FAILURE;

public class SubscribeHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(SubscribeHandler.class);

    private SessionStore sessionStore;
    private MessagePublisher messagePublisher;
    private Authenticator authenticator;

    public SubscribeHandler(SessionStore sessionStore, MessagePublisher messagePublisher, Authenticator authenticator) {
        this.sessionStore = sessionStore;
        this.messagePublisher = messagePublisher;
        this.authenticator = authenticator;
    }

    @Override
    public void handle(Channel channel, MqttMessage m) {
        MqttSubscribeMessage msg = (MqttSubscribeMessage) m;
        String clientId = NettyUtils.clientId(channel);
        int packetId = MessageUtils.packetId(msg);

        String username = NettyUtils.username(channel);
        List<MqttTopicSubscription> subs = checkSubscriptions(clientId, username, msg);

        List<Subscription> newSubscriptions = storeSubscription(subs, clientId);

        MqttSubAckMessage ackMessage = MessageUtils.createSubAck(subs, packetId);
        channel.writeAndFlush(ackMessage).addListener(FIRE_EXCEPTION_ON_FAILURE);

        // fire the persisted messages in session
        for (Subscription subscription : newSubscriptions) {
            messagePublisher.publishRetainedMessages(subscription);
        }
    }

    private List<MqttTopicSubscription> checkSubscriptions(String clientId, String username, MqttSubscribeMessage msg) {
        int packetId = MessageUtils.packetId(msg);
        List<MqttTopicSubscription> subs = new ArrayList<>();
        for (MqttTopicSubscription sub : msg.payload().topicSubscriptions()) {
            String topicFilter = sub.topicName();
            MqttQoS qos;
            if (!authenticator.canRead(topicFilter, username)) {
                // send SUBACK with 0x80, the user hasn't credentials to read the topic
                log.warn("Client does not have readable permission, clientId={}, username={}, packetId={}, topicFilter={}.",
                        clientId, username, packetId, topicFilter);
                qos = FAILURE;
            } else {
                if (MessageUtils.isValidTopic(topicFilter)) {
                    log.info("Topic filter is valid, clientId={}, username={}, packetId={}, topicFilter={}.",
                            clientId, username, packetId, topicFilter);
                    qos = sub.qualityOfService();
                } else {
                    log.warn("Topic filter is not valid. clientId={}, username={}, packetId={}, topicFilter={}.",
                            clientId, username, packetId, topicFilter);
                    qos = FAILURE;
                }
            }
            subs.add(new MqttTopicSubscription(topicFilter, qos));
        }
        return subs;
    }

    private List<Subscription> storeSubscription(List<MqttTopicSubscription> subs, String clientId) {
        List<Subscription> subscriptions = new ArrayList<>();
        for (MqttTopicSubscription sub : subs) {
            if (sub.qualityOfService() != FAILURE) {
                Subscription newSubscription = new Subscription(clientId, sub.topicName(), sub.qualityOfService().value());
                sessionStore.storeSubscription(clientId, newSubscription);
                log.info("Store subscription into SubscriptionStore. clientId={}, newSubscription={}", clientId, newSubscription);
                subscriptions.add(newSubscription);
            }
        }
        return subscriptions;
    }

}
