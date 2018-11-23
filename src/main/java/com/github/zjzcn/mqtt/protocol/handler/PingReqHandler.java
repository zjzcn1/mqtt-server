package com.github.zjzcn.mqtt.protocol.handler;

import com.github.zjzcn.mqtt.protocol.MessageHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

public class PingReqHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PingReqHandler.class);

    @Override
    public void handle(Channel channel, MqttMessage msg) {
        MqttFixedHeader pingHeader = new MqttFixedHeader(
                MqttMessageType.PINGRESP,
                false,
                AT_MOST_ONCE,
                false,
                0);
        MqttMessage pingResp = new MqttMessage(pingHeader);
        channel.writeAndFlush(pingResp);
    }

}
