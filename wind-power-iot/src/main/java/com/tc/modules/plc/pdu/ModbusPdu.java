package com.tc.modules.plc.pdu;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/** Super-interface for objects that can be encoded as a Modbus PDU. */
public interface ModbusPdu {

  /**
   * Get the function code identifying this PDU.
   *
   * @return the function code identifying this PDU.
   */
  int getFunctionCode();
}
