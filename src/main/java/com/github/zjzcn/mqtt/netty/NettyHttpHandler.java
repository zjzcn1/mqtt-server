package com.github.zjzcn.mqtt.netty;

import com.github.zjzcn.mqtt.util.Utils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(NettyHttpHandler.class);

    private static Map<String, String> DEFAULT_MIME_TYPES = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("txt", "text/plain");
            put("css", "text/css");
            put("csv", "text/csv");
            put("htm", "text/html");
            put("html", "text/html");
            put("xml", "text/xml");
            put("js", "text/javascript");
            put("xhtml", "application/xhtml+xml");
            put("json", "application/json");
            put("pdf", "application/pdf");
            put("zip", "application/zip");
            put("tar", "application/x-tar");
            put("gif", "image/gif");
            put("jpeg", "image/jpeg");
            put("jpg", "image/jpeg");
            put("tiff", "image/tiff");
            put("tif", "image/tiff");
            put("png", "image/png");
            put("svg", "image/svg+xml");
            put("ico", "image/x-icon");
            put("mp3", "audio/mpeg");
        }
    };

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(httpRequest)) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                ctx.write(response);
                return;
            }
            QueryStringDecoder decoderQuery = new QueryStringDecoder(httpRequest.uri());
            String path = decoderQuery.path();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            path = Utils.getRootPath() + "webapp" + path;
            File file = new File(path);
            if (!file.exists()) {
                log.error("404 Not found, path={}.", path);
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.NOT_FOUND,
                        Unpooled.copiedBuffer("404 Not found!".getBytes()));
                ctx.write(response);
                return;
            }
            InputStream in = new FileInputStream(path);
            ByteArrayOutputStream out = copyStream(in);
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(out.toByteArray()));
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, guessMimeType(path));
            ctx.write(httpResponse);
        } catch (Throwable t) {
            log.error("An exception was caught while handling http request.", t);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer("500 Server internal error!".getBytes()));
            ctx.write(response);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
        log.error("An exception was caught.", t);
        ctx.close();
    }

    private ByteArrayOutputStream copyStream(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        return out;
    }

    private String guessMimeType(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        String extension = path.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        String mimeType = DEFAULT_MIME_TYPES.get(extension);
        if (mimeType == null) {
            return "";
        }
        return mimeType;
    }
}
