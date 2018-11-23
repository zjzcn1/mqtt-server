package com.github.zjzcn.mqtt.security;

import java.util.Properties;

public interface Authenticator {

    void init(Properties properties);

    boolean allowAnonymous();

    boolean login(String clientId, String username, byte[] password);

    boolean canWrite(String topic, String username);

    boolean canRead(String topicFilter, String username);
}
