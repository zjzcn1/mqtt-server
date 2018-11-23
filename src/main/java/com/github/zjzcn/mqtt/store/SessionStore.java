package com.github.zjzcn.mqtt.store;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public interface SessionStore {

    void init(Properties properties);

    int nextPacketId(String clientId);

    void storeSession(String clientId, Session session);

    Session getSession(String clientId);

    void cleanSession(String clientId);

    void storeSubscription(String clientId, Subscription subscription);

    Subscription removeSubscription(String clientId, String topicFilter);

    Collection<Subscription> getSubscriptions(String clientId);

    Collection<Subscription> searchSubscriptions(String topic);

    void storeInboundMessage(String clientId, Integer packetId, StoredMessage message);

    StoredMessage removeInboundMessage(String clientId, Integer packetId);

    void storeOutboundMessage(String clientId, Integer packetId, StoredMessage message);

    StoredMessage removeOutboundMessage(String clientId, Integer packetId);

    Map<Integer, StoredMessage> getOutboundMessages(String clientId);

    void storeSecondPhaseMessage(String clientId, Integer packetId, PubRelMessage message);

    PubRelMessage removeSecondPhaseMessage(String clientId, Integer packetId);

    Map<Integer, PubRelMessage> getSecondPhaseMessages(String clientId);

    void storeRetainedMessage(String topic, RetainedMessage message);

    Collection<RetainedMessage> searchRetainedMessages(String topicFilter);

    RetainedMessage removeRetainedMessage(String topic);

}
