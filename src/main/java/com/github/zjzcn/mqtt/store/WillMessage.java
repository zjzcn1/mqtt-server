package com.github.zjzcn.mqtt.store;

import java.io.Serializable;

public class WillMessage implements Serializable {

    private static final long serialVersionUID = -1L;

    private String willTopic;
    private int willQos;
    private boolean willRetain;
    private byte[] willPayload;

    public int getWillQos() {
        return willQos;
    }

    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    public byte[] getWillPayload() {
        return willPayload;
    }

    public void setWillPayload(byte[] willPayload) {
        this.willPayload = willPayload;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }


    @Override
    public String toString() {
        return String.format(
                "StoredMessage[willTopic=%s, willQos=%s, willRetain=%s]",
                this.willTopic,
                this.willQos,
                this.willRetain);
    }
}
