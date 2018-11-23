package com.github.zjzcn.mqtt.store;

import java.io.Serializable;
import java.util.Objects;

public class Session implements Serializable {

    private static final long serialVersionUID = -1L;

    private String clientId;
    private boolean cleanSession;

    private WillMessage willMessage;

    public Session(String clientId, boolean cleanSession) {
        this.clientId = clientId;
        this.cleanSession = cleanSession;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public WillMessage getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(WillMessage willMessage) {
        this.willMessage = willMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Session that = (Session) o;
        return Objects.equals(this.clientId, that.getClientId());
    }

    @Override
    public int hashCode() {
        return clientId != null ? clientId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Session[clientId=%s, cleanSession=%s]",
                this.clientId,
                this.cleanSession);
    }
}