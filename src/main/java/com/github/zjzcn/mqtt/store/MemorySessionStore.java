package com.github.zjzcn.mqtt.store;

import com.github.zjzcn.mqtt.protocol.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemorySessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(MemorySessionStore.class);

    // Map<clientId, Session>
    private Map<String, Session> sessions = new ConcurrentHashMap<>();
    // Map<clientId, Map<topicFilter, Subscription>>
    private Map<String, Map<String, Subscription>> subscriptions = new ConcurrentHashMap<>();
    // Map<topic, RetainedMessage>
    private Map<String, RetainedMessage> retainedMessages = new ConcurrentHashMap<>();
    // Map<clientId, Map<packetId, StoredMessage>>
    private Map<String, Map<Integer, StoredMessage>> inboundMessages = new ConcurrentHashMap<>();
    private Map<String, Map<Integer, StoredMessage>> outboundMessages = new ConcurrentHashMap<>();
    // Map<clientId, Map<packetId, PubRelMessage>>
    private Map<String, Map<Integer, PubRelMessage>> secondPhaseMessages = new ConcurrentHashMap<>();
    // Map<clientId, AtomicInteger>
    private Map<String, AtomicInteger> packetIdGenerators = new ConcurrentHashMap<>();

    @Override
    public void init(Properties properties) {
        // NOOP
    }

    @Override
    public int nextPacketId(String clientId) {
        return packetIdGenerators.get(clientId).getAndIncrement();
    }

    @Override
    public synchronized void storeSession(String clientId, Session session) {
        sessions.put(clientId, session);
        if (!packetIdGenerators.containsKey(clientId)) {
            packetIdGenerators.put(clientId, new AtomicInteger(1));
        }
        if (!subscriptions.containsKey(clientId)) {
            subscriptions.put(clientId, new ConcurrentHashMap<>());
        }
        if (!inboundMessages.containsKey(clientId)) {
            inboundMessages.put(clientId, new ConcurrentHashMap<>());
        }
        if (!outboundMessages.containsKey(clientId)) {
            outboundMessages.put(clientId, new ConcurrentHashMap<>());
        }
        if (!secondPhaseMessages.containsKey(clientId)) {
            secondPhaseMessages.put(clientId, new ConcurrentHashMap<>());
        }
    }

    @Override
    public Session getSession(String clientId) {
        return sessions.get(clientId);
    }

    @Override
    public synchronized void cleanSession(String clientId) {
        sessions.remove(clientId);
        subscriptions.remove(clientId);
        secondPhaseMessages.remove(clientId);
        inboundMessages.remove(clientId);
        outboundMessages.remove(clientId);
    }

    @Override
    public void storeSubscription(String clientId, Subscription sub) {
        subscriptions.get(clientId).put(sub.getTopicFilter(), sub);
    }

    @Override
    public Subscription removeSubscription(String clientId, String topicFilter) {
        Map<String, Subscription> subs = subscriptions.get(clientId);
        return subs.remove(topicFilter);
    }

    @Override
    public Collection<Subscription> getSubscriptions(String clientId) {
        return subscriptions.get(clientId).values();
    }

    @Override
    public Collection<Subscription> searchSubscriptions(String topic) {
        Collection<Subscription> allSubs = new HashSet<>();
        Collection<Map<String, Subscription>> values = subscriptions.values();
        for (Map<String, Subscription> subs : values) {
            allSubs.addAll(subs.values());
        }

        Collection<Subscription> results = new HashSet<>();
        for (Subscription sub : allSubs) {
            if (MessageUtils.isMatchTopic(topic, sub.getTopicFilter())) {
                results.add(sub);
            }
        }
        return results;
    }

    @Override
    public void storeInboundMessage(String clientId, Integer packetId, StoredMessage message) {
        inboundMessages.get(clientId).put(packetId, message);
    }

    @Override
    public StoredMessage removeInboundMessage(String clientId, Integer packetId) {
        return inboundMessages.get(clientId).remove(packetId);
    }

    @Override
    public void storeOutboundMessage(String clientId, Integer packetId, StoredMessage message) {
        outboundMessages.get(clientId).put(packetId, message);
    }

    @Override
    public StoredMessage removeOutboundMessage(String clientId, Integer packetId) {
        return outboundMessages.get(clientId).remove(packetId);
    }

    @Override
    public Map<Integer, StoredMessage> getOutboundMessages(String clientId) {
        return outboundMessages.get(clientId);
    }

    @Override
    public void storeSecondPhaseMessage(String clientId, Integer packetId, PubRelMessage message) {
        secondPhaseMessages.get(clientId).put(packetId, message);
    }

    @Override
    public PubRelMessage removeSecondPhaseMessage(String clientId, Integer packetId) {
        return secondPhaseMessages.get(clientId).remove(packetId);
    }

    @Override
    public Map<Integer, PubRelMessage> getSecondPhaseMessages(String clientId) {
        return secondPhaseMessages.get(clientId);
    }

    @Override
    public void storeRetainedMessage(String topic, RetainedMessage message) {
        retainedMessages.put(topic, message);
    }

    @Override
    public Collection<RetainedMessage> searchRetainedMessages(String topicFilter) {
        Collection<RetainedMessage> results = new HashSet<>();
        for (String topic : retainedMessages.keySet()) {
            if (MessageUtils.isMatchTopic(topic, topicFilter)) {
                results.add(retainedMessages.get(topic));
            }
        }
        return results;
    }

    @Override
    public RetainedMessage removeRetainedMessage(String topic) {
        return retainedMessages.remove(topic);
    }
}
