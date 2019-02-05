package com.omb.streaming;

import com.omb.camera.CameraSource;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.rtsp.RtspDecoder;
import io.netty.handler.codec.rtsp.RtspEncoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RtspServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtspServer.class);
    private CameraSource cameraSource;

    public RtspServer(CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try (InputStream jpgIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("struggle.jpg")) {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new RtspDecoder(), new RtspEncoder());
                    p.addLast(new RtspServerHandler(() -> IOUtils.toByteArray(jpgIS)));
                }
            });

            Channel ch = b.bind(8554).sync().channel();
            LOGGER.info("Connect to rtsp://127.0.0.1:8554");
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("RTSP server error", e);
        } catch (IOException e) {
            LOGGER.error("Error reading image, e");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}