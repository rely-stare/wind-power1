package com.tc.modules.hk.netsdk;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class PlayCtrlManager {

    private final Map<Integer, PlayCtrl> playCtrlMap = new ConcurrentHashMap<>();
    private final Map<Integer, IntByReference> portMap = new ConcurrentHashMap<>();

    public PlayCtrl createPlayCtrl() {
        PlayCtrl playCtrl = (PlayCtrl)Native.loadLibrary(getPlayCtrlPath(), PlayCtrl.class);
        return playCtrl;
    }

    public void bindPlayHandle(int playHandle, PlayCtrl playCtrl, IntByReference port) {
        playCtrlMap.put(playHandle, playCtrl);
        portMap.put(playHandle, port);
    }

    public PlayCtrl getPlayCtrl(int playHandle) {
        PlayCtrl playCtrl = playCtrlMap.get(playHandle);
        if (playCtrl == null) {
            throw new IllegalStateException("PlayCtrl实例不存在，playHandle=" + playHandle);
        }
        return playCtrl;
    }

    public IntByReference getPort(int playHandle) {
        IntByReference port = portMap.get(playHandle);
        if (port == null) {
            throw new IllegalStateException("Port不存在，playHandle=" + playHandle);
        }
        return port;
    }

    public void removePlayHandle(int playHandle) {
        playCtrlMap.remove(playHandle);
        portMap.remove(playHandle);
    }

    private String getPlayCtrlPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userDir = System.getProperty("user.dir");
        if (os.contains("win")) {
            return Paths.get(userDir, "lib", "hk", "win", "PlayCtrl.dll").toString();
        } else if (os.contains("linux")) {
            return Paths.get(userDir, "lib", "hk", "linux", "libPlayCtrl.so").toString();
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }
}
