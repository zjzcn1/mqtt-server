package com.github.zjzcn.mqtt.store;

import java.io.Serializable;

public class RetainedMessage implements Serializable {

    private static final long serialVersionUID = -1L;

    private String topic;
    private int qos;
    private byte[] payload;

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


    @Override
    public String toString() {
        return String.format(
                "RetainedMessage[topic=%s, qos=%s]",
                this.topic,
                this.qos);
    }
}
