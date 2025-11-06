package com.tc.modules.plc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Slf4j
public class ModbusClientHandler extends ChannelInboundHandlerAdapter {


    private final ConnectionListener connectionListener;
    private final Map<Integer, Promise<byte[]>> pendingRequests = new ConcurrentHashMap<>();


    public ModbusClientHandler(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] response = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(response);
        // 解析事务 ID（假设事务 ID 是响应的前 2 个字节）
        int transactionId = ((response[0] & 0xFF) << 8 | (response[1] & 0xFF));
        // 找到对应的 Promise 并设置结果
        Promise<byte[]> promise = pendingRequests.remove(transactionId);
        if (promise != null && !promise.isDone()) {
            promise.setSuccess(response);
        }
        byteBuf.release();
    }

    public void addPromise(int transactionId, Promise<byte[]> promise) {
        pendingRequests.put(transactionId, promise);
    }

    /**
     * 当通道激活时被调用，用于处理连接成功的情况
     *
     * @param ctx 通道处理上下文，提供了访问通道、管道等组件的方法
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (connectionListener != null) {
            connectionListener.onConnect();
        }
    }

    /**
     * 处理通道非激活事件时的逻辑
     * 当通道（Channel）变得非激活时，这个方法会被调用
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (connectionListener != null) {
            connectionListener.onDisconnect();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if (connectionListener != null) {
            connectionListener.onException(cause);
        }
    }
}
