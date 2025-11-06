package com.tc.modules.message.sse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {

    // 用 Map 来管理 hardwareId -> userId -> SseEmitter
    private static final Map<String, Map<String, SseEmitter>> SPEED_DEVICE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, SseEmitter>> TEMPERATURE_DEVICE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, SseEmitter>> FLUCTUATE_DEVICE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, SseEmitter>> AVERAGE_DEVICE_CACHE = new ConcurrentHashMap<>();

    // 获取连接，按 hardwareId 和 userId 区分
    public SseEmitter getConnSpeed(@NotBlank String hardwareId, @NotBlank String userId) {
        Map<String, SseEmitter> clientEmitters = SPEED_DEVICE_CACHE.computeIfAbsent(hardwareId, k -> new ConcurrentHashMap<>());
        final SseEmitter emitter = clientEmitters.get(userId);

        if (emitter != null) {
            return emitter;
        } else {
            // 设置连接超时时间
            final SseEmitter newEmitter = new SseEmitter(-1L);

            // 注册完成回调
            newEmitter.onCompletion(() -> {
                log.info("连接已关闭，正准备释放，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    SPEED_DEVICE_CACHE.remove(hardwareId);  // 如果该设备没有其他连接，移除设备
                }
                log.info("连接已释放，hardwareId = {}, userId = {}", hardwareId, userId);
            });

            // 注册异常回调
            newEmitter.onError(throwable -> {
                log.error("连接已异常，正准备关闭，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    SPEED_DEVICE_CACHE.remove(hardwareId);
                }
            });

            clientEmitters.put(userId, newEmitter);
            return newEmitter;
        }
    }

    // 获取连接，按 hardwareId 和 userId 区分
    public SseEmitter getConnTemperature(@NotBlank String hardwareId, @NotBlank String userId) {
        Map<String, SseEmitter> clientEmitters = TEMPERATURE_DEVICE_CACHE.computeIfAbsent(hardwareId, k -> new ConcurrentHashMap<>());
        final SseEmitter emitter = clientEmitters.get(userId);

        if (emitter != null) {
            return emitter;
        } else {
            // 设置连接超时时间
            final SseEmitter newEmitter = new SseEmitter(-1L);

            // 注册完成回调
            newEmitter.onCompletion(() -> {
                log.info("连接已关闭，正准备释放，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    TEMPERATURE_DEVICE_CACHE.remove(hardwareId);  // 如果该设备没有其他连接，移除设备
                }
                log.info("连接已释放，hardwareId = {}, userId = {}", hardwareId, userId);
            });

            // 注册异常回调
            newEmitter.onError(throwable -> {
                log.error("连接已异常，正准备关闭，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    TEMPERATURE_DEVICE_CACHE.remove(hardwareId);
                }
            });

            clientEmitters.put(userId, newEmitter);
            return newEmitter;
        }
    }

    public SseEmitter getConnAverage(@NotBlank String hardwareId, @NotBlank String userId) {
        Map<String, SseEmitter> clientEmitters = AVERAGE_DEVICE_CACHE.computeIfAbsent(hardwareId, k -> new ConcurrentHashMap<>());
        final SseEmitter emitter = clientEmitters.get(userId);

        if (emitter != null) {
            return emitter;
        } else {
            // 设置连接超时时间
            final SseEmitter newEmitter = new SseEmitter(-1L);

            // 注册完成回调
            newEmitter.onCompletion(() -> {
                log.info("连接已关闭，正准备释放，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    AVERAGE_DEVICE_CACHE.remove(hardwareId);  // 如果该设备没有其他连接，移除设备
                }
                log.info("连接已释放，hardwareId = {}, userId = {}", hardwareId, userId);
            });

            // 注册异常回调
            newEmitter.onError(throwable -> {
                log.error("连接已异常，正准备关闭，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    AVERAGE_DEVICE_CACHE.remove(hardwareId);
                }
            });

            clientEmitters.put(userId, newEmitter);
            return newEmitter;
        }
    }

    // 发送数据给指定设备的所有客户端
    public void sendToAllSpeedClients(@NotBlank String hardwareId, Object msg) {
        Map<String, SseEmitter> clientEmitters = SPEED_DEVICE_CACHE.get(hardwareId);
        if (clientEmitters != null) {
            for (String userId : clientEmitters.keySet()) {
                SseEmitter emitter = clientEmitters.get(userId);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().data(msg));
                    } catch (IOException e) {
                        log.error("发送数据失败，hardwareId = {}, userId = {}", hardwareId, userId);
                        emitter.completeWithError(e);
                    }
                } else {
                    log.warn("连接已关闭或找不到连接，hardwareId = {}, userId = {}", hardwareId, userId);
                }
            }
        }
    }

    // 发送数据给指定设备的所有客户端
    public void sendToAllTempClients(@NotBlank String hardwareId, Object msg) {
        Map<String, SseEmitter> clientEmitters = TEMPERATURE_DEVICE_CACHE.get(hardwareId);
        if (clientEmitters != null) {
            for (String userId : clientEmitters.keySet()) {
                SseEmitter emitter = clientEmitters.get(userId);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().data(msg));
                    } catch (IOException e) {
                        log.error("发送数据失败，hardwareId = {}, userId = {}", hardwareId, userId);
                        emitter.completeWithError(e);
                    }
                } else {
                    log.warn("连接已关闭或找不到连接，hardwareId = {}, userId = {}", hardwareId, userId);
                }
            }
        }
    }

    // 关闭指定设备的所有连接
    public void closeConnSpeed(@NotBlank String hardwareId, @NotBlank String userId) {
        Map<String, SseEmitter> clientEmitters = SPEED_DEVICE_CACHE.get(hardwareId);
        if (clientEmitters != null) {
            SseEmitter sseEmitter = clientEmitters.get(userId);
            if (sseEmitter != null) {
                sseEmitter.complete();
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    SPEED_DEVICE_CACHE.remove(hardwareId);
                }
            }
        }
    }

    public SseEmitter getSpeedFluctuateConn(String hardwareId, String userId) {
        Map<String, SseEmitter> clientEmitters = FLUCTUATE_DEVICE_CACHE.computeIfAbsent(hardwareId, k -> new ConcurrentHashMap<>());
        final SseEmitter emitter = clientEmitters.get(userId);

        if (emitter != null) {
            return emitter;
        } else {
            // 设置连接超时时间
            final SseEmitter newEmitter = new SseEmitter(-1L);

            // 注册完成回调
            newEmitter.onCompletion(() -> {
                log.info("连接已关闭，正准备释放，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    FLUCTUATE_DEVICE_CACHE.remove(hardwareId);  // 如果该设备没有其他连接，移除设备
                }
                log.info("连接已释放，hardwareId = {}, userId = {}", hardwareId, userId);
            });

            // 注册异常回调
            newEmitter.onError(throwable -> {
                log.error("连接已异常，正准备关闭，hardwareId = {}, userId = {}", hardwareId, userId);
                clientEmitters.remove(userId);
                if (clientEmitters.isEmpty()) {
                    FLUCTUATE_DEVICE_CACHE.remove(hardwareId);
                }
            });

            clientEmitters.put(userId, newEmitter);
            return newEmitter;
        }
    }

    public void sendToAllFluctuateClients(@NotBlank String hardwareId, Object msg) {
        Map<String, SseEmitter> clientEmitters = FLUCTUATE_DEVICE_CACHE.get(hardwareId);
        if (clientEmitters != null) {
            for (String userId : clientEmitters.keySet()) {
                SseEmitter emitter = clientEmitters.get(userId);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().data(msg));
                    } catch (IOException e) {
                        log.error("发送数据失败，hardwareId = {}, userId = {}", hardwareId, userId);
                        emitter.completeWithError(e);
                    }
                } else {
                    log.warn("连接已关闭或找不到连接，hardwareId = {}, userId = {}", hardwareId, userId);
                }
            }
        }
    }

    public void sendToAllAverageClients(@NotBlank String hardwareId, Object msg) {
        Map<String, SseEmitter> clientEmitters = AVERAGE_DEVICE_CACHE.get(hardwareId);
        if (clientEmitters != null) {
            for (String userId : clientEmitters.keySet()) {
                SseEmitter emitter = clientEmitters.get(userId);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().data(msg));
                    } catch (IOException e) {
                        log.error("发送数据失败，hardwareId = {}, userId = {}", hardwareId, userId);
                        emitter.completeWithError(e);
                    }
                } else {
                    log.warn("连接已关闭或找不到连接，hardwareId = {}, userId = {}", hardwareId, userId);
                }
            }
        }
    }

}

