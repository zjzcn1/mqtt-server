package com.github.zjzcn.mqtt;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.github.zjzcn.mqtt.common.MqttConfig;
import com.github.zjzcn.mqtt.netty.NettyAcceptor;
import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.store.SessionStore;
import com.github.zjzcn.mqtt.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Properties;

public class MqttServer {

    private static final Logger log = LoggerFactory.getLogger(MqttServer.class);

    private static final String CONFIG_FILE = "conf/config.yaml";
    private static final String LOGBACK_XML_FILE = "conf/logback.xml";

    private NettyAcceptor acceptor;

    public static void main(String[] args) {
        String rootPath = Utils.getRootPath();
        initLogback(rootPath);
        MqttConfig config = loadConfig(rootPath);

        MqttServer server = new MqttServer();
        server.start(config);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    public void start(MqttConfig config) {
        long start = System.currentTimeMillis();

        SessionStore sessionStore = Utils.forObject(config.getSessionStoreClass());
        assert sessionStore != null;
        sessionStore.init(config.getProperties());
        Authenticator authenticator = Utils.forObject(config.getAuthenticatorClass());
        assert authenticator != null;
        authenticator.init(config.getProperties());

        acceptor = new NettyAcceptor(sessionStore, authenticator);
        acceptor.start(config);

        long time = System.currentTimeMillis() - start;
        log.info("Mqtt Server has been started successfully in {} ms", time);
    }

    public void stop() {
        acceptor.close();
        log.info("Mqtt Server has been stopped.");
    }

    private static MqttConfig loadConfig(String rootPath) {
        MqttConfig mqttConfig = new MqttConfig();
        String fileName = rootPath + CONFIG_FILE;
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(new FileInputStream(fileName));
            log.info("Loaded config file, path={} \n{}", fileName, yaml.dump(config));
            Properties prop = new Properties();
            prop.putAll(config);
            mqttConfig.setProperties(prop);
            mqttConfig.setHost((String)config.get("host"));
            mqttConfig.setMqttPort((Integer)config.get("mqtt_port"));
            mqttConfig.setMqttWsPort((Integer)config.get("mqtt_ws_port"));
            mqttConfig.setHttpPort((Integer)config.get("http_port"));
            mqttConfig.setSessionStoreClass(Utils.forClass((String)(config.get("session_store_class"))));
            mqttConfig.setAuthenticatorClass(Utils.forClass((String)(config.get("authenticator_class"))));
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFoundException: ", e);
            System.exit(-1);
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException: ", e);
            System.exit(-1);
        }
        return mqttConfig;
    }

    private static void initLogback(String rootPath) {
        File file = new File(rootPath + LOGBACK_XML_FILE);
        if (!file.exists()) {
            System.err.println("Not find logback config, path= " + file);
            System.exit(-1);
            return;
        }
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(ctx);
        ctx.reset();
        try {
            configurator.doConfigure(file);
            log.info("Loaded logback config, path={}.", file);
        } catch (JoranException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

}
