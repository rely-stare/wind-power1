package com.tc.modules.plc.client;

import com.tc.modules.plc.handler.ConnectionListener;
import com.tc.modules.plc.handler.ModbusClientHandler;
import com.tc.modules.plc.pdu.*;
import com.tc.utils.ModbusUtil;
import com.tc.utils.TransactionIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Slf4j
public class ModbusTcpClient {

    private final String host;
    private final int port;
    private final int slaveId;
    private Channel channel;
    private ConnectionListener connectionListener;

    private ModbusTcpClient(String host, int port ,int slaveId) {
        this.host = host;
        this.port = port;
        this.slaveId = slaveId;
    }


    public void addConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public static ModbusTcpClient create(String host, int port, int slaveId) {
        return new ModbusTcpClient(host, port, slaveId);
    }

    public void connect() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(
                                new LengthFieldBasedFrameDecoder(1024, 4, 2, 0, 0),
                                new ByteArrayEncoder(),
                                new ModbusClientHandler(connectionListener)
                        );
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();

    }

    public void disconnect() {
        if (channel != null) {
            channel.close();
        }
    }


    public synchronized byte[] sendRequest(byte[] request) throws InterruptedException, ExecutionException {
        Promise<byte[]> promise = channel.eventLoop().newPromise();
        int transactionId = ((request[0] & 0xFF) << 8) | (request[1] & 0xFF);
        // 获取 handler 进行赋值
        ModbusClientHandler handler = (ModbusClientHandler) channel.pipeline().last();
        handler.addPromise(transactionId, promise);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                promise.setFailure(future.cause());
            }
        });
        return promise.get();  // 阻塞直到获取响应
    }

    // 多个线程下 不好用
    public void sendRequestAsync(byte[] request, Promise<byte[]> promise) {
        ModbusClientHandler handler = (ModbusClientHandler) channel.pipeline().last();
        int transactionId = ((request[0] & 0xFF) << 8) | (request[1] & 0xFF);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("Failed to send request", future.cause());
                promise.setFailure(future.cause());
            }else{
                handler.addPromise(transactionId, promise);
            }
        });
    }

    public ReadHoldingRegistersResponse readHoldingRegisters(ReadHoldingRegistersRequest request) throws Exception{
        ByteBuf readBuf = Unpooled.buffer(256);

        readBuf.clear();
        // Transaction Identifier
        readBuf.writeShort(TransactionIdGenerator.generateTransactionId());
        // Protocol Identifier
        readBuf.writeShort(0x0000);
        // Length Field
        readBuf.writeShort(6);
        // slaveId
        readBuf.writeByte(slaveId);

        ReadHoldingRegistersRequest.encode(request, readBuf);

        // 获取 ByteBuf 的字节数组
        byte[] array = new byte[readBuf.readableBytes()];
        readBuf.getBytes(readBuf.readerIndex(), array);

        readBuf.release();

        byte[] bytes = sendRequest(array);
        return ReadHoldingRegistersResponse.parseResponse(bytes);
    }

    public WriteSingleRegisterResponse writeSingleRegister(WriteSingleRegisterRequest request) throws Exception{

        ByteBuf writeBuf = Unpooled.buffer(256);

        writeBuf.clear();
        // Transaction Identifier
        writeBuf.writeShort(TransactionIdGenerator.generateTransactionId());
        // Protocol Identifier
        writeBuf.writeShort(0x0000);
        // Length Field
        writeBuf.writeShort(6);
        // slaveId
        writeBuf.writeByte(slaveId);

        WriteSingleRegisterRequest.encode(request, writeBuf);

        // 获取 ByteBuf 的字节数组
        byte[] array = new byte[writeBuf.readableBytes()];
        writeBuf.getBytes(writeBuf.readerIndex(), array);

        byte[] bytes = sendRequest(array);
        // 释放 ByteBuf
        writeBuf.release();
        return WriteSingleRegisterResponse.parseResponse(bytes);
    }


    public WriteMultipleRegistersResponse writeMultipleRegisters(
            int startAddress,
            float[] floatValues,
            boolean bigEndian
    ) throws ExecutionException, InterruptedException {
        // 每个 float 2 个寄存器
        int totalRegs = floatValues.length * 2;
        int[] allRegisters = new int[totalRegs];

        for (int i = 0; i < floatValues.length; i++) {
            int[] regs = ModbusUtil.floatToRegisters(floatValues[i], bigEndian);
            allRegisters[i * 2] = regs[0];
            allRegisters[i * 2 + 1] = regs[1];
        }

        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(startAddress, allRegisters);

        ByteBuf writeBuf = Unpooled.buffer(256);
        int txId = TransactionIdGenerator.generateTransactionId();

        // 写入 MBAP Header
        writeBuf.writeShort(txId);     // Transaction ID
        writeBuf.writeShort(0);        // Protocol ID
        writeBuf.writeShort(7 + allRegisters.length * 2); // Length
        writeBuf.writeByte(slaveId);   // Unit ID

        WriteMultipleRegistersRequest.encode(request, writeBuf);

        // 取出字节数组
        byte[] array = new byte[writeBuf.readableBytes()];
        writeBuf.getBytes(writeBuf.readerIndex(), array);
        writeBuf.release();

        // 发送请求（使用 sendRequestAsync）并解析响应
        sendRequest(array);
        return WriteMultipleRegistersResponse.parseResponse(array);
    }


}
