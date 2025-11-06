//package com.tc;
//
//import com.serotonin.modbus4jQ.ModbusFactory;
//import com.serotonin.modbus4j.ModbusMaster;
//import com.serotonin.modbus4j.exception.ModbusTransportException;
//import com.serotonin.modbus4j.ip.IpParameters;
//import com.tc.modules.plc.entity.ModbusClientEntity;
//import com.tc.utils.ModbusUtil;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.PooledByteBufAllocator;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//@SpringBootTest
//@Slf4j
//class WindPowerIotApplicationTests {
//
//	private static final int offset = 0;
//
//
//	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);
//
//	@Test
//	void contextLoads() {
//		ModbusClientEntity entity = new ModbusClientEntity("127.0.0.1", 502, 1, "1");
//
//		IpParameters address = new IpParameters();
//		address.setHost(entity.getHost());
//		address.setPort(entity.getPort());
//		ModbusMaster master = new ModbusFactory().createTcpMaster(address, false);
//
//		ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(4);
//
//
//		ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
//			try {
//				long start = System.currentTimeMillis();
//				com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest registersRequest = new com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest(entity.getSlaveId(), offset + 10, 9);
//				com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse registersResponse = (com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse) master.send(registersRequest);
//				short[] registers = registersResponse.getShortData();
//				log.debug("读取转速数据耗时: {} ms", System.currentTimeMillis() - start);
//
//			} catch (ModbusTransportException e) {
//				throw new RuntimeException(e);
//			}
//		}, 0, 100, TimeUnit.MILLISECONDS);
//
//
//		ScheduledFuture<?> scheduledFuture1 = executorService.scheduleAtFixedRate(() -> {
//			try {
//				long start = System.currentTimeMillis();
//				com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest registersRequest = new com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest(entity.getSlaveId(), offset + 50, 40);
//				com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse registersResponse = (com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse) master.send(registersRequest);
//				short[] registers = registersResponse.getShortData();
//
//				long end = System.currentTimeMillis();
//				log.debug("读取长时温度耗时: {} ms", end - start);
//
//				com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest registersRequest2 = new com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest(entity.getSlaveId(), offset + 90, 15);
//				com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse registersResponse2 = (com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse) master.send(registersRequest);
//				short[] registers2 = registersResponse.getShortData();
//
//				long last = System.currentTimeMillis();
//				log.debug("读取超温耗时: {} ms", last - end);
//
//
//			} catch (ModbusTransportException e) {
//				throw new RuntimeException(e);
//			}
//		}, 0, 1000, TimeUnit.MILLISECONDS);
//	}
//
//}
