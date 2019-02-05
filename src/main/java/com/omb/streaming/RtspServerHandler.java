package com.omb.streaming;

import com.omb.camera.CameraSource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspHeaderValues;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspResponseStatuses;
import io.netty.handler.codec.rtsp.RtspVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RtspServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtspServer.class);
    private CameraSource cameraSource;
    ByteBuf buf;

    public RtspServerHandler(CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void sendAnswer(ChannelHandlerContext ctx, DefaultHttpRequest req, FullHttpResponse rep) {
        final String cseq = req.headers().get(RtspHeaderNames.CSEQ);
        if (cseq != null) {
            rep.headers().add(RtspHeaderNames.CSEQ, cseq);
        }
        final String session = req.headers().get(RtspHeaderNames.SESSION);
        if (session != null) {
            rep.headers().add(RtspHeaderNames.SESSION, session);
        }
        if (!HttpHeaders.isKeepAlive(req)) {
            ctx.write(rep).addListener(ChannelFutureListener.CLOSE);
        } else {
            rep.headers().set(RtspHeaderNames.CONNECTION, RtspHeaderValues.KEEP_ALIVE);
            ctx.write(rep);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof DefaultHttpRequest) {

            DefaultHttpRequest req = (DefaultHttpRequest) msg;

            FullHttpResponse rep = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, RtspResponseStatuses.NOT_FOUND);
            if (req.method() == RtspMethods.OPTIONS) {
                rep.setStatus(RtspResponseStatuses.OK);
                rep.headers().add(RtspHeaderValues.PUBLIC, "DESCRIBE, SETUP, PLAY, TEARDOWN");
                sendAnswer(ctx, req, rep);
            } else if (req.method() == RtspMethods.DESCRIBE) {
                try {
                    if(buf==null) {
                        buf = Unpooled.copiedBuffer(cameraSource.getBytes());
                    }
                } catch (IOException e) {
                    LOGGER.error("could not read bytes, e");
                }
                rep.setStatus(RtspResponseStatuses.OK);
                rep.headers().add(RtspHeaderNames.CONTENT_TYPE, "application/sdp");
                rep.headers().add(RtspHeaderNames.CONTENT_LENGTH, buf.writerIndex());
                rep.content().writeBytes(buf);
                sendAnswer(ctx, req, rep);
            } else if (req.method() == RtspMethods.SETUP) {
                rep.setStatus(RtspResponseStatuses.OK);
                String session = String.format("%08x", (int) (Math.random() * 65536));
                rep.headers().add(RtspHeaderNames.SESSION, session);
                rep.headers().add(RtspHeaderNames.TRANSPORT, "RTP/AVP;unicast;client_port=5004-5005");
                sendAnswer(ctx, req, rep);
            } else if (req.method() == RtspMethods.PLAY) {
                rep.setStatus(RtspResponseStatuses.OK);
                sendAnswer(ctx, req, rep);
            } else {
                System.err.println("Not managed :" + req.method());
                ctx.write(rep).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}