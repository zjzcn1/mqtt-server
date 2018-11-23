package com.github.zjzcn.mqtt.security;

import java.util.Properties;

public class PermitAllAuthenticator implements Authenticator {

    @Override
    public void init(Properties properties) {
        // NOOP
    }

    @Override
    public boolean allowAnonymous() {
        return true;
    }

    @Override
    public boolean login(String clientId, String username, byte[] password) {
        return true;
    }

    @Override
    public boolean canWrite(String topic, String username) {
        return true;
    }

    @Override
    public boolean canRead(String topicFilter, String username) {
        return true;
    }
}
