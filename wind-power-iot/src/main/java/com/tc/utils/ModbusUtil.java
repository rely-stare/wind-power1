package com.tc.utils;

public class ModbusUtil {

    /**
     * 检查 Modbus 寄存器中的指定 bit 位
     *
     * @param registerValue Modbus 寄存器的值
     * @param bitIndex 要检查的 bit 位的索引（从 0 开始）
     * @return 如果指定的 bit 为 1，则返回 true；否则返回 false
     */
    public static boolean getBitStatus(int registerValue, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 15) {
            throw new IllegalArgumentException("bitIndex must be between 0 and 15");
        }
        // 通过位运算判断指定 bit 位的值
        return (registerValue & (1 << bitIndex)) != 0;
    }

    /**
     * 检查 Modbus 寄存器中的指定 bit 位
     *
     * @param registerValue Modbus 寄存器的值
     * @param bitIndex 要检查的 bit 位的索引（从 0 开始）
     * @param isLittleEndian 是否使用小端字节序（如果为 true，则高字节在前；如果为 false，则低字节在前）
     * @return 如果指定的 bit 为 1，则返回 true；否则返回 false
     */
    public static boolean getBitStatus(int registerValue, int bitIndex, boolean isLittleEndian) {
        if (bitIndex < 0 || bitIndex > 15) {
            throw new IllegalArgumentException("bitIndex must be between 0 and 15");
        }

        if (isLittleEndian) {
            // 小端模式下，低字节在前，高字节在后
            // 低8位和高8位要对调
            int lowByte = registerValue & 0xFF;            // 低字节
            int highByte = (registerValue >> 8) & 0xFF;     // 高字节
            registerValue = (lowByte << 8) | highByte;      // 交换高低字节
        }

        // 判断bit位状态
        return (registerValue & (1 << bitIndex)) != 0;
    }

    /**
     * 修改寄存器的某一位，并返回修改后的值
     * @param registerValue 16 位寄存器值
     * @param bitIndex 位索引 (0-15)
     * @param bitValue 要设置的值 (0 或 1)
     * @return 修改后的寄存器值
     */
    public static int writeBit(int registerValue, int bitIndex, int bitValue) {
        if (bitIndex < 0 || bitIndex > 15) {
            throw new IllegalArgumentException("bitIndex must be between 0 and 15");
        }
        if (bitValue == 1) {
            return registerValue | (1 << bitIndex);  // 设置某位为 1
        } else {
            return registerValue & ~(1 << bitIndex); // 设置某位为 0
        }
    }



    /**
     * 将浮点数转换为两个 16 位寄存器的表示形式
     * 此方法将一个 32 位浮点数转换为 IEEE 754 格式的两个 16 位整数（寄存器），根据指定的字节顺序排列
     *
     * @param value 要转换的浮点数值
     * @param bigEndian 指定是否使用大端字节序如果为 true，则高字节在前；如果为 false，则低字节在前
     * @return 包含两个 16 位整数的数组，表示原始浮点数的寄存器值
     */
    public static int[] floatToRegisters(float value, boolean bigEndian) {
        // 将浮点数转换为 IEEE 754 格式的整数
        int intBits = Float.floatToIntBits(value);

        // 分离高 16 位和低 16 位
        int highRegister = (intBits >> 16) & 0xFFFF; // 高 16 位
        int lowRegister = intBits & 0xFFFF;          // 低 16 位

        // 返回寄存器数组，按字节顺序排列
        return bigEndian
                ? new int[] { highRegister, lowRegister }  // 大端：高位在前
                : new int[] { lowRegister, highRegister }; // 小端：低位在前
    }

}
