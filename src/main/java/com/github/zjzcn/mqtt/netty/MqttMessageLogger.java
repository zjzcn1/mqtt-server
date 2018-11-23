package com.github.zjzcn.mqtt.netty;

import com.github.zjzcn.mqtt.protocol.MessageUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

@Sharable
public class MqttMessageLogger extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageLogger.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        doLog(ctx, message, "C-->S");
        ctx.fireChannelRead(message);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        doLog(ctx, msg, "S-->C");
        ctx.write(msg, promise).addListener(CLOSE_ON_FAILURE);
    }

    private void doLog(ChannelHandlerContext ctx, Object message, String direction) {
        if (!(message instanceof MqttMessage)) {
            return;
        }
        ChannelId channelId = ctx.channel().id();
        MqttMessage msg = (MqttMessage) message;
        String clientId = NettyUtils.clientId(ctx.channel());
        MqttMessageType messageType = msg.fixedHeader().messageType();
        int qos = msg.fixedHeader().qosLevel().value();
        switch (messageType) {
            case PINGREQ:
            case PINGRESP:
                log.debug("{} [{}] clientId={}, qos={}, channelId={}.", direction, messageType, clientId, qos, channelId);
                break;
            case CONNECT:
                MqttConnectMessage conn = (MqttConnectMessage) msg;
                clientId = conn.payload().clientIdentifier();
                log.info("{} [{}] clientId={}, qos={}, channelId={}.", direction, messageType, clientId, qos, channelId);
                break;
            case CONNACK:
            case DISCONNECT:
                log.info("{} [{}] clientId={}, qos={}, channelId={}.", direction, messageType, clientId, qos, channelId);
                break;
            case SUBSCRIBE:
                MqttSubscribeMessage subscribe = (MqttSubscribeMessage) msg;
                log.info("{} [{}] clientId={}, qos={}, topicFilters={}, channelId={}.", direction, messageType, clientId, qos,
                        subscribe.payload().topicSubscriptions(), channelId);
                break;
            case SUBACK:
                MqttSubAckMessage subAck = (MqttSubAckMessage) msg;
                final List<Integer> grantedQoSLevels = subAck.payload().grantedQoSLevels();
                log.debug("{} [{}] clientId={}, qos={}, packetId={}, grantedQoSLevels={}, channelId={}.", direction, messageType,
                        clientId, qos, MessageUtils.packetId(msg), grantedQoSLevels, channelId);
                break;
            case UNSUBSCRIBE:
                MqttUnsubscribeMessage unsubscribe = (MqttUnsubscribeMessage) msg;
                log.info("{} [{}] clientId={}, qos={}, topicFilters={}, channelId={}.", direction, messageType, clientId, qos,
                        unsubscribe.payload().topics(), channelId);
                break;
            case UNSUBACK:
                log.info("{} [{}] clientId={}, qos={}, packetId={}, channelId={}.", direction, messageType, clientId, qos,
                        MessageUtils.packetId(msg), channelId);
                break;
            case PUBLISH:
                MqttPublishMessage publish = (MqttPublishMessage) msg;
                log.debug("{} [{}] clientId={}, qos={}, topic={}, channelId={}.", direction, messageType, clientId, qos,
                        publish.variableHeader().topicName(), channelId);
                break;
            case PUBACK:
            case PUBREC:
            case PUBREL:
            case PUBCOMP:
                log.debug("{} [{}] clientId={}, qos={}, packetId={}, channelId={}.", direction, messageType, clientId, qos,
                        MessageUtils.packetId(msg), channelId);
                break;
        }
    }

}
