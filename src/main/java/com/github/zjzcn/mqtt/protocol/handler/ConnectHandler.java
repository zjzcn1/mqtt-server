package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.netty.NettyUtils;
import com.github.zjzcn.mqtt.protocol.ChannelManager;
import com.github.zjzcn.mqtt.protocol.MessageHandler;
import com.github.zjzcn.mqtt.protocol.MessagePublisher;
import com.github.zjzcn.mqtt.protocol.MessageUtils;
import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.store.Session;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.store.Subscription;
import com.github.zjzcn.mqtt.store.WillMessage;
import com.github.zjzcn.mqtt.util.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;
import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;

public class ConnectHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ConnectHandler.class);

    private SessionStore sessionStore;
    private ChannelManager channelManager;
    private MessagePublisher messagePublisher;
    private Authenticator authenticator;

    public ConnectHandler(SessionStore sessionStore, Authenticator authenticator, ChannelManager channelManager,
                          MessagePublisher messagePublisher) {
        this.sessionStore = sessionStore;
        this.channelManager = channelManager;
        this.messagePublisher = messagePublisher;
        this.authenticator = authenticator;
    }

    @Override
    public void handle(Channel channel, MqttMessage m) {
        MqttConnectMessage msg = (MqttConnectMessage) m;
        MqttConnectPayload payload = msg.payload();
        String clientId = payload.clientIdentifier();
        final String username = payload.userName();

        int version = msg.variableHeader().version();
        if (version != MqttVersion.MQTT_3_1.protocolLevel() && version != MqttVersion.MQTT_3_1_1.protocolLevel()) {
            channel.writeAndFlush(MessageUtils.createConnAck(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION))
                    .addListener(FIRE_EXCEPTION_ON_FAILURE);
            channel.close().addListener(CLOSE_ON_FAILURE);
            log.error("MQTT protocol version is not valid, clientId={}.", clientId);
            return;
        }

        boolean cleanSession = msg.variableHeader().isCleanSession();
        if (clientId == null || clientId.length() == 0) {
            if (!cleanSession) {
                channel.writeAndFlush(MessageUtils.createConnAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED))
                        .addListener(FIRE_EXCEPTION_ON_FAILURE);
                channel.close().addListener(CLOSE_ON_FAILURE);
                log.error("MQTT clientId cannot be empty, cleanSession=false, username={}.", username);
                return;
            }
            // Generating client id.
            clientId = Utils.uuid();
            log.info("Client has connected with server generated id={}, username={}.", clientId, username);
        }

        if (!login(channel, msg, clientId)) {
            channel.writeAndFlush(MessageUtils.createConnAck(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD))
                    .addListener(FIRE_EXCEPTION_ON_FAILURE);
            channel.close().addListener(CLOSE_ON_FAILURE);
            return;
        }

        log.info("Put channel to channelManager, clientId={}, channelId={}.", clientId, channel.id());
        NettyUtils.clientId(channel, clientId);

        Session session = sessionStore.getSession(clientId);
        boolean isSessionPresent = false;
        if (session != null) {
            isSessionPresent = true;
            if (cleanSession) {
                sessionStore.cleanSession(clientId);
                log.info("Cleaned session from SessionStore, clientId={}, session={}.", clientId, session);
            } else {
                log.debug("Reauthorizing subscriptions, clientId={}.", clientId);
                reauthorizeExistingSubscriptions(clientId, username);
            }

            Channel oldChannel = channelManager.getChannel(clientId);
            if (oldChannel != null) {
                oldChannel.close().addListener(CLOSE_ON_FAILURE);
            }
        }

        Session newSession = new Session(clientId, cleanSession);
        if (msg.variableHeader().isWillFlag()) {
            WillMessage will = MessageUtils.asWillMessage(msg);
            newSession.setWillMessage(will);
            log.info("Set MQTT will message, clientId={}, willMessage={}.", clientId, will);
        }

        sessionStore.storeSession(clientId, newSession);
        channelManager.putChannel(clientId, channel);

        setKeepAliveTimeout(channel, msg, clientId);

        MqttConnAckMessage connAck = createConnAck(msg, isSessionPresent);
        channel.writeAndFlush(connAck);

        if (!cleanSession) {
            messagePublisher.republishStoredMessage(clientId);
        }
    }

    private void reauthorizeExistingSubscriptions(String clientId, String username) {
        if (sessionStore.getSession(clientId) == null) {
            return;
        }
        Collection<Subscription> subscriptions = sessionStore.getSubscriptions(clientId);
        log.info("Get subscriptions from SubscriptionStore, clientId={}, subscriptions={}.", clientId, subscriptions);
        for (Subscription sub : subscriptions) {
            String topicFilter = sub.getTopicFilter();
            boolean readAuthorized = authenticator.canRead(topicFilter, username);
            if (!readAuthorized) {
                sessionStore.removeSubscription(clientId, topicFilter);
                log.warn("Removed subscription from SubscriptionStore, clientId={}, subscription={}.", clientId, sub);
            }
        }
    }

    private boolean login(Channel channel, MqttConnectMessage msg, final String clientId) {
        if (authenticator.allowAnonymous()) {
            log.info("Server is allowAnonymous, client login success, clientId={}.", clientId);
            return true;
        }
        if (msg.variableHeader().hasUserName()) {
            byte[] password = null;
            if (msg.variableHeader().hasPassword()) {
                password = msg.payload().passwordInBytes();
            }
            String username = msg.payload().userName();
            if (authenticator.login(clientId, username, password)) {
                NettyUtils.username(channel, username);
                log.info("Client login success, clientId={}.", clientId);
                return true;
            } else {
                log.error("Client login failed, bad username or password, clientId={}, username={}.", clientId, username);
                return false;
            }
        } else {
            log.error("Client login failed, have not username, clientId={}.", clientId);
            return false;
        }
    }

    private MqttConnAckMessage createConnAck(MqttConnectMessage msg, boolean isSessionPresent) {
        MqttConnAckMessage okResp;
        boolean cleanSession = msg.variableHeader().isCleanSession();
        if (!cleanSession && isSessionPresent) {
            okResp = MessageUtils.createConnAck(CONNECTION_ACCEPTED, true);
        } else {
            okResp = MessageUtils.createConnAck(CONNECTION_ACCEPTED);
        }
        return okResp;
    }

    private void setKeepAliveTimeout(Channel channel, MqttConnectMessage msg, String clientId) {
        int keepAlive = msg.variableHeader().keepAliveTimeSeconds();
        int idleTime = Math.round(keepAlive * 1.5f);
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.names().contains("idleStateHandler")) {
            pipeline.remove("idleStateHandler");
        }
        pipeline.addFirst("idleStateHandler", new IdleStateHandler(idleTime, 0, 0));
        log.debug("KeepAlive has been configured, clientId={}, keepAlive={}, cleanSession={}, timeoutTime={}.",
                clientId, keepAlive, msg.variableHeader().isCleanSession(), idleTime);
    }

}
