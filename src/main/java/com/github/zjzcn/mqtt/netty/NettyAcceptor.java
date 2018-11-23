package com.github.zjzcn.mqtt.netty;

import com.github.zjzcn.mqtt.common.MqttConfig;
import com.github.zjzcn.mqtt.security.Authenticator;
import com.github.zjzcn.mqtt.store.SessionStore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

public class NettyAcceptor {

    private static final String MQTT_SUB_PROTOCOL_LIST = "mqtt, mqttv3.1, mqttv3.1.1";

    private abstract static class PipelineInitializer {
        abstract void init(SocketChannel channel) throws Exception;
    }

    private static final Logger log = LoggerFactory.getLogger(NettyAcceptor.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private SessionStore sessionStore;
    private Authenticator authenticator;

    public NettyAcceptor(SessionStore sessionStore, Authenticator authenticator) {
        this.sessionStore = sessionStore;
        this.authenticator = authenticator;
    }

    public void start(MqttConfig config) {
        log.debug("Starting Netty acceptor.");

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        NettyMqttHandler mqttHandler = new NettyMqttHandler(sessionStore, authenticator);

        startTcpServer(mqttHandler, config.getHost(), config.getMqttPort());

        startWebSocketServer(mqttHandler, config.getHost(), config.getMqttWsPort());

        startHttpServer(config.getHost(), config.getHttpPort());
    }

    private void initFactory(String host, int port, String protocol, final PipelineInitializer pipeline) {
        log.debug("Starting server, protocol={}.", protocol);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        pipeline.init(ch);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(host, port);
            log.info("Server bound to host={}, port={}, protocol={}.", host, port, protocol);
            f.sync().addListener(FIRE_EXCEPTION_ON_FAILURE);
        } catch (InterruptedException e) {
            log.error("An exception was caught while starting server, protocol={}.", protocol, e);
        }
    }

    private void startTcpServer(NettyMqttHandler handler, String host, int port) {
        initFactory(host, port, "TCP MQTT", new PipelineInitializer() {
            @Override
            void init(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addFirst("idleStateHandler", new IdleStateHandler(10, 0, 0));
                pipeline.addLast("decoder", new MqttDecoder(8092));
                pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                pipeline.addLast("messageLogger", new MqttMessageLogger());
                pipeline.addLast("handler", handler);
            }
        });
    }

    private void startWebSocketServer(NettyMqttHandler handler, String host, int webSocketPort) {
        initFactory(host, webSocketPort, "WebSocket MQTT", new PipelineInitializer() {
            @Override
            void init(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("compressor ", new HttpContentCompressor());
                pipeline.addLast("protocol", new WebSocketServerProtocolHandler("/mqtt", MQTT_SUB_PROTOCOL_LIST));
                pipeline.addLast("webSocketCodec", new MqttWebSocketCodec());
                pipeline.addFirst("idleStateHandler", new IdleStateHandler(10, 0, 0));
                pipeline.addLast("decoder", new MqttDecoder(8092));
                pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                pipeline.addLast("messageLogger", new MqttMessageLogger());
                pipeline.addLast("handler", handler);
            }
        });
    }

    private void startHttpServer(String host, int httpPort) {
        initFactory(host, httpPort, "Http Server", new PipelineInitializer() {
            @Override
            void init(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("compressor ", new HttpContentCompressor());
                pipeline.addLast("handler", new NettyHttpHandler());
            }
        });
    }

    public void close() {
        log.debug("Closing Netty acceptor...");
        if (workerGroup == null || bossGroup == null) {
            log.error("Netty acceptor is not initialized.");
            throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized.");
        }
        Future<?> workerWaiter = workerGroup.shutdownGracefully();
        Future<?> bossWaiter = bossGroup.shutdownGracefully();

        /*
         * We shouldn't raise an IllegalStateException if we are interrupted. If we did so, the
         * broker is not shut down properly.
         */
        log.info("Waiting for worker and boss event loop groups to terminate...");
        try {
            workerWaiter.await(10, TimeUnit.SECONDS);
            bossWaiter.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException iex) {
            log.warn("An InterruptedException was caught while waiting for event loops to terminate...");
        }

        if (!workerGroup.isTerminated()) {
            log.warn("Forcing shutdown of worker event loop...");
            workerGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }

        if (!bossGroup.isTerminated()) {
            log.warn("Forcing shutdown of boss event loop...");
            bossGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }
    }

}
