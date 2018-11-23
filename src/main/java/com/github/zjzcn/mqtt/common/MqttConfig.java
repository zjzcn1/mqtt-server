package com.github.zjzcn.mqtt.common;

import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.security.PermitAllAuthenticator;
import com.github.zjzcn.mqtt.store.MemorySessionStore;
import com.github.zjzcn.mqtt.store.SessionStore;

import java.util.Properties;

public final class MqttConfig {

    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int DEFAULT_MQTT_PORT = 1883;
    public static final int DEFAULT_MQTT_WS_PORT = 8080;
    public static final Class<? extends SessionStore> DEFAULT_STORE_CLASS = MemorySessionStore.class;
    public static final Class<? extends Authenticator> DEFAULT_AUTHENTICATOR_CLASS = PermitAllAuthenticator.class;

    private Properties properties;

    private String host;
    private int mqttPort;
    private int mqttWsPort;
    private int httpPort;

    private Class<? extends SessionStore> sessionStoreClass;
    private Class<? extends Authenticator> authenticatorClass;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getMqttPort() {
        return mqttPort;
    }

    public void setMqttPort(int mqttPort) {
        this.mqttPort = mqttPort;
    }

    public int getMqttWsPort() {
        return mqttWsPort;
    }

    public void setMqttWsPort(int mqttWsPort) {
        this.mqttWsPort = mqttWsPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public Class<? extends SessionStore> getSessionStoreClass() {
        return sessionStoreClass;
    }

    public void setSessionStoreClass(Class<? extends SessionStore> sessionStoreClass) {
        this.sessionStoreClass = sessionStoreClass;
    }

    public Class<? extends Authenticator> getAuthenticatorClass() {
        return authenticatorClass;
    }

    public void setAuthenticatorClass(Class<? extends Authenticator> authenticatorClass) {
        this.authenticatorClass = authenticatorClass;
    }
}
