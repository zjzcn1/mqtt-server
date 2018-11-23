package com.github.zjzcn.mqtt.protocol;


import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface MessageHandler {

    void handle(Channel channel, MqttMessage msg);
}
