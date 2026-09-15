package com.psy.framework.web.service;

import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务：生成图片验证码并缓存校验（4位数字）
 */
@Service
public class CaptchaService {

    /** 验证码缓存（uuid -> code） */
    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();

    /** 验证码有效期（分钟） */
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000;

    /** 验证码缓存（uuid -> 过期时间） */
    private final Map<String, Long> captchaExpire = new ConcurrentHashMap<>();

    /**
     * 生成验证码图片，返回 {uuid, img(base64), code}
     */
    public Map<String, String> getCaptchaImage() {
        int width = 130;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int num = random.nextInt(10);
            code.append(num);
            g.setColor(new Color(30 + random.nextInt(120), 60 + random.nextInt(120), 120 + random.nextInt(120)));
            g.drawString(String.valueOf(num), 20 + i * 26, 28);
        }

        // 干扰线
        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(120 + random.nextInt(120), 120 + random.nextInt(120), 120 + random.nextInt(120)));
            g.drawLine(0, random.nextInt(height), width, random.nextInt(height));
        }
        g.dispose();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());

            String uuid = UUID.randomUUID().toString().replace("-", "");
            captchaStore.put(uuid, code.toString());
            captchaExpire.put(uuid, System.currentTimeMillis() + EXPIRE_MILLIS);
            clearExpired();

            Map<String, String> result = new java.util.HashMap<>();
            result.put("uuid", uuid);
            result.put("img", base64);
            return result;
        } catch (Exception e) {
            throw new ServiceException("验证码生成失败");
        }
    }

    /**
     * 校验验证码（校验后即失效）
     */
    public boolean verify(String uuid, String code) {
        if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(code)) {
            return false;
        }
        Long expire = captchaExpire.get(uuid);
        if (expire == null || expire < System.currentTimeMillis()) {
            captchaStore.remove(uuid);
            captchaExpire.remove(uuid);
            return false;
        }
        String saved = captchaStore.get(uuid);
        captchaStore.remove(uuid);
        captchaExpire.remove(uuid);
        return saved != null && saved.equalsIgnoreCase(code.trim());
    }

    /** 清理过期验证码 */
    private void clearExpired() {
        long now = System.currentTimeMillis();
        captchaExpire.forEach((key, expire) -> {
            if (expire < now) {
                captchaStore.remove(key);
                captchaExpire.remove(key);
            }
        });
    }
}
