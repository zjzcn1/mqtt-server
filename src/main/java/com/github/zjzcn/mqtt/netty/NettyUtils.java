package com.github.zjzcn.mqtt.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public final class NettyUtils {

    private static final AttributeKey<Object> ATTR_KEY_CLIENT_ID = AttributeKey.valueOf("clientId");
    private static final AttributeKey<Object> ATTR_KEY_USERNAME = AttributeKey.valueOf("username");

    public static void clientId(Channel channel, String clientID) {
        channel.attr(NettyUtils.ATTR_KEY_CLIENT_ID).set(clientID);
    }

    public static String clientId(Channel channel) {
        return (String) channel.attr(NettyUtils.ATTR_KEY_CLIENT_ID).get();
    }

    public static void username(Channel channel, String username) {
        channel.attr(NettyUtils.ATTR_KEY_USERNAME).set(username);
    }

    public static String username(Channel channel) {
        return (String) channel.attr(NettyUtils.ATTR_KEY_USERNAME).get();
    }

    public static byte[] byteBuf2bytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        int idx = byteBuf.readerIndex();
        byteBuf.readBytes(bytes);
        byteBuf.readerIndex(idx);
        return bytes;
    }

    public static ByteBuf bytes2byteBuf(byte[] bytes) {
        return Unpooled.copiedBuffer(bytes);
    }

    public static String channelId(Channel channel) {
        return channel.id().asShortText();
    }
}
