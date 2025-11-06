package com.tc.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * Author jiangzhou
 * Date 2023/10/24
 * Description TODO
 **/
public class TokenUtils {

    /**
     * JWT 签名密钥
     */
    private static final String JWT_SECRET = "1!2@3#4$5%6^7&8*9(0)";

    private static final long expirationMillis = 3600000L * 24 * 365 * 5;//5 years

    public static String generate(String userId) {
        Date expiration = new Date(new Date().getTime() + expirationMillis);

        return Jwts.builder()
                .setExpiration(expiration)
                .setSubject(userId)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }

    public static String getUserId(String token) {
        try {
            // 解析并验证JWT令牌
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET) // 替换为您使用的密钥
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

}
