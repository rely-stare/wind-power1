package com.tc.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionIdGenerator {

    private static final AtomicInteger transactionIdCounter = new AtomicInteger(0);

    // 获取下一个事务 ID，支持回绕
    public static int generateTransactionId() {
        // 保证最大值为 65535
        return transactionIdCounter.getAndIncrement() & 0xFFFF;
    }
}
