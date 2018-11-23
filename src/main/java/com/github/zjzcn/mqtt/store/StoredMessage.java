package com.github.zjzcn.mqtt.store;

import java.io.Serializable;

public class StoredMessage implements Serializable {

    private static final long serialVersionUID = -1L;

    private String topic;
    private int qos;
    private boolean retained;
    private byte[] payload;
    private int packetId;

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public String toString() {
        return String.format(
                "StoredMessage[topic=%s, qos=%s, retained=%s, packetId=%s, payloadLength=%s]",
                this.topic,
                this.qos,
                this.retained,
                this.packetId,
                this.payload.length);
    }
}
