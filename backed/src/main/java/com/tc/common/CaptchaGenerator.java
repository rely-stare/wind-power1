package com.tc.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author jiangzhou
 * Date 2023/11/9
 * Description TODO
 **/
@Component
public class CaptchaGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int WIDTH = 200;
    private static final int HEIGHT = 50;
    private static final int CODE_LENGTH = 6;

    private static final String prefix = "validate:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateCaptchaImage(String ip) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            char c = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
            code.append(c);
            int x = 20 * i + 10 + random.nextInt(10);
            int y = 20 + random.nextInt(20);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString(String.valueOf(c), x, y);
        }

        String captcha = code.toString();
        // 验证码一分钟失效
        redisTemplate.opsForValue().set(prefix + ip, captcha, 1, TimeUnit.MINUTES);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
