package com.psy.framework.security;

import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌服务（JWT 无状态令牌，参照若依 TokenService 简化实现）
 */
@Component
public class TokenService {

    /** 令牌密钥 */
    @Value("${token.secret}")
    private String secret;

    /** 令牌有效期（分钟） */
    @Value("${token.expire-time}")
    private Long expireTime;

    /**
     * 创建令牌
     */
    public String createToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", loginUser.getUserId());
        claims.put("username", loginUser.getUsername());
        claims.put("roleKey", loginUser.getRoleKey());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime * 60 * 1000);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(loginUser.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析令牌，返回登录用户信息
     */
    public LoginUser parseToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            LoginUser loginUser = new LoginUser();
            Object userId = claims.get("userId");
            loginUser.setUserId(userId == null ? null : Long.valueOf(userId.toString()));
            loginUser.setUsername(claims.getSubject());
            Object roleKey = claims.get("roleKey");
            loginUser.setRoleKey(roleKey == null ? null : roleKey.toString());
            return loginUser;
        } catch (Exception e) {
            throw new ServiceException("登录状态已过期，请重新登录");
        }
    }
}
