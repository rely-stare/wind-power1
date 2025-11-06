package com.tc.modules.plc.handler;

public interface ConnectionListener {

    void onConnect();

    void onDisconnect();

    void onException(Throwable cause);

}
