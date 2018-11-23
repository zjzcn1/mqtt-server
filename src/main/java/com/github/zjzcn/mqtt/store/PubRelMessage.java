package com.github.zjzcn.mqtt.store;

import java.io.Serializable;

public class PubRelMessage implements Serializable {

    private static final long serialVersionUID = -1L;

    private String clientId;
    private int packetId;

    public PubRelMessage(String clientId, int packetId) {
        this.clientId = clientId;
        this.packetId = packetId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
                "PubRelMessage[clientId=%s, packetId=%s]",
                this.clientId,
                this.packetId);
    }
}
