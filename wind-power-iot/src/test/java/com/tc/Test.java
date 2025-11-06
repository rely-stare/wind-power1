//package com.tc;
//
//import com.digitalpetri.modbus.client.ModbusTcpClient;
//import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
//import com.digitalpetri.modbus.exceptions.ModbusResponseException;
//import com.digitalpetri.modbus.exceptions.ModbusTimeoutException;
//import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
//import com.digitalpetri.modbus.pdu.ReadHoldingRegistersResponse;
//import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Scanner;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//public class Test {
//
//    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);
//
//
//    public static void main(String[] args) throws ModbusExecutionException, ModbusTimeoutException, ModbusResponseException {
//        NettyTcpClientTransport nettyTcpClientTransport = NettyTcpClientTransport.create(cfg -> {
//            cfg.hostname = "127.0.0.1";
//            cfg.port = 502;
//        });
//
//        ModbusTcpClient client = ModbusTcpClient.create(nettyTcpClientTransport);
//        client.connect();
//
//        executorService.scheduleAtFixedRate(() -> {
//
//            ReadHoldingRegistersRequest readHoldingRegistersRequest = new ReadHoldingRegistersRequest(0, 10);
//
//            try {
//                long l = System.currentTimeMillis();
//                ReadHoldingRegistersResponse response = client.readHoldingRegisters(1, readHoldingRegistersRequest);
//                log.info("读取数据耗时: {} ms", System.currentTimeMillis() - l);
//
//            } catch (ModbusExecutionException | ModbusResponseException | ModbusTimeoutException e) {
//               log.error("读取数据异常", e);
//            }
//
//        }, 0, 100, TimeUnit.MILLISECONDS);
//
//
//        while (true){
//            Scanner scanner = new Scanner(System.in);
//            String next = scanner.next();
//            if ("exit".equals(next)){
//                client.disconnect();
//                break;
//            }
//        }
//    }
//}
