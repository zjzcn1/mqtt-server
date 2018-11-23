package com.github.zjzcn.mqtt.netty;

import com.github.zjzcn.mqtt.protocol.ChannelManager;
import com.github.zjzcn.mqtt.protocol.MessagePublisher;
import com.github.zjzcn.mqtt.protocol.handler.*;
import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.store.SessionStore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

@Sharable
public class NettyMqttHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(NettyMqttHandler.class);

    private MessagePublisher messagePublisher;
    private ChannelManager channelManager;

    private ConnectHandler connectHandler;
    private DisconnectHandler disconnectHandler;
    private PingReqHandler pingReqHandler;
    private PubAckHandler pubAckHandler;
    private PubCompHandler pubCompHandler;
    private PublishHandler publishHandler;
    private PubRecHandler pubRecHandler;
    private PubRelHandler pubRelHandler;
    private SubscribeHandler subscribeHandler;
    private UnsubscribeHandler unsubscribeHandler;

    public NettyMqttHandler(SessionStore sessionStore, Authenticator authenticator) {
        channelManager = new ChannelManager();
        messagePublisher = new MessagePublisher(sessionStore, channelManager);

        connectHandler = new ConnectHandler(sessionStore, authenticator, channelManager, messagePublisher);
        disconnectHandler = new DisconnectHandler(sessionStore, channelManager);
        pingReqHandler = new PingReqHandler();
        pubAckHandler = new PubAckHandler(sessionStore);
        pubCompHandler = new PubCompHandler(sessionStore);
        publishHandler = new PublishHandler(sessionStore, authenticator, messagePublisher);
        pubRecHandler = new PubRecHandler(sessionStore);
        pubRelHandler = new PubRelHandler(sessionStore, messagePublisher);
        subscribeHandler = new SubscribeHandler(sessionStore, messagePublisher, authenticator);
        unsubscribeHandler = new UnsubscribeHandler(sessionStore);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        Channel channel = ctx.channel();
        MqttMessage msg = (MqttMessage) message;
        if (msg.fixedHeader() == null) {
        	throw new IOException("Unknown packet");
        }
        MqttMessageType messageType = msg.fixedHeader().messageType();
        String clientId = NettyUtils.clientId(ctx.channel());
        if (MqttMessageType.CONNECT != messageType && StringUtils.isEmpty(clientId)) {
            log.warn("Illegal request, closing Netty channel, messageType={}, clientId={}.", messageType, clientId);
            ctx.close().addListener(CLOSE_ON_FAILURE);
            return;
        }
        try {
            switch (messageType) {
                case CONNECT:
                    connectHandler.handle(channel, msg);
                    break;
                case SUBSCRIBE:
                    subscribeHandler.handle(channel, msg);
                    break;
                case UNSUBSCRIBE:
                    unsubscribeHandler.handle(channel, msg);
                    break;
                case PUBLISH:
                    publishHandler.handle(channel, msg);
                    break;
                case PUBACK:
                    pubAckHandler.handle(channel, msg);
                    break;
                case PUBREC:
                    pubRecHandler.handle(channel, msg);
                    break;
                case PUBREL:
                    pubRelHandler.handle(channel, msg);
                    break;
                case PUBCOMP:
                    pubCompHandler.handle(channel, msg);
                    break;
                case DISCONNECT:
                    disconnectHandler.handle(channel, msg);
                    break;
                case PINGREQ:
                    pingReqHandler.handle(channel, msg);
                    break;
                default:
                    log.error("Unknown MessageType: {}", messageType);
                    break;
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String clientId = NettyUtils.clientId(ctx.channel());
        log.warn("Netty channelInactive Event: channel is closed, clientId={}, channelId={}.", clientId, ctx.channel().id());
        if (StringUtils.isNotEmpty(clientId)) {
            channelManager.removeChannel(clientId);
            messagePublisher.publishWillMessageToSubscribers(clientId);
        }
        ctx.close().addListener(CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        log.error("Netty exceptionCaught Event: channelId={}.", ctx.channel().id(), e);
        if (e instanceof IOException) {
            // Connection reset by peer
            ctx.close().addListener(CLOSE_ON_FAILURE);
        } else {
            super.exceptionCaught(ctx, e);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String clientId = NettyUtils.clientId(ctx.channel());
        if (evt instanceof IdleStateEvent) {
            IdleState e = ((IdleStateEvent) evt).state();
            if (e == IdleState.READER_IDLE) {
                log.info("Firing channel inactive event. clientId = {}.", clientId);
                // fire a channelInactive to trigger publish of Will
                ctx.fireChannelInactive();
                ctx.close().addListener(CLOSE_ON_FAILURE);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Firing Netty event. clientId = {}, eventClass = {}.", clientId, evt.getClass().getName());
            }
            super.userEventTriggered(ctx, evt);
        }
    }

}
