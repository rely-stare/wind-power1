package com.tc.modules.message.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * websocket
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}/{deviceId}")  // 设备和用户连接
public class WebSocketServer {

    private Session session;

    // Map<deviceId, Map<userId, Session>>：记录每个机组对应的用户连接
    private static Map<String, Map<String, Session>> deviceSessionPool = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("deviceId") String deviceId) {
        try {
            this.session = session;
            // 将每个用户的连接放入对应机组的用户列表中
            deviceSessionPool.computeIfAbsent(deviceId, k -> new HashMap<>()).put(userId, session);
            log.info("【websocket消息】机组 {} 与用户 {} 连接成功，当前连接数: {}", deviceId, userId, deviceSessionPool.size());
        } catch (Exception e) {
            log.error("【websocket消息】连接失败: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId, @PathParam("deviceId") String deviceId) {
        try {
            Map<String, Session> userSessions = deviceSessionPool.get(deviceId);
            if (userSessions != null) {
                userSessions.remove(userId);
                log.info("【websocket消息】机组 {} 与用户 {} 连接断开，当前连接数: {}", deviceId, userId, userSessions.size());
            }
        } catch (Exception ignored) {
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("【websocket消息】收到客户端消息: {}", message);
        session.getAsyncRemote().sendText("");
    }

    @OnError
    public void onError(Throwable throwable) {
        log.error("【webSocketServer】发生错误: {}", throwable.getMessage());
    }

    // 推送单个机组数据给所有连接该机组的用户
    public void sendToDeviceUsers(String deviceId, String message) {
        Map<String, Session> userSessions = deviceSessionPool.get(deviceId);
        if (userSessions != null) {
            for (Session session : userSessions.values()) {
                if (session.isOpen()) {
                    try {
                        log.debug("【websocket消息】向机组 {} 的所有连接用户发送消息: {}", deviceId, message);
                        session.getAsyncRemote().sendText(message);
                    } catch (Exception e) {
                        log.error("【websocket消息】发送消息失败: {}", e.getMessage());
                    }
                }
            }
        }
    }
}
