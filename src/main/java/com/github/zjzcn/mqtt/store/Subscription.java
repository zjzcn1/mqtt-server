package com.github.zjzcn.mqtt.store;

import java.io.Serializable;
import java.util.Objects;

public final class Subscription implements Serializable {

    private static final long serialVersionUID = -1L;

    private final String clientId;
    private final String topicFilter;
    private final int qos;

    public Subscription(String clientId, String topicFilter, int qos) {
        this.qos = qos;
        this.clientId = clientId;
        this.topicFilter = topicFilter;
    }

    public int getQos() {
        return qos;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription that = (Subscription) o;
        if (!Objects.equals(clientId, that.getClientId())) {
            return false;
        }
        return Objects.equals(topicFilter, that.getTopicFilter());
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (topicFilter != null ? topicFilter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "Subscription[topicFilter=%s, clientId=%s, qos=%s]",
                this.topicFilter,
                this.clientId,
                this.qos);
    }
}
