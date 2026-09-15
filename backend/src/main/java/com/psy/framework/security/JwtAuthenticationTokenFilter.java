package com.psy.framework.security;

import com.psy.common.constant.Constants;
import com.psy.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器：解析请求头令牌并写入当前登录用户
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.isNotEmpty(authHeader) && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
            try {
                LoginUser loginUser = tokenService.parseToken(token);
                if (loginUser != null) {
                    SecurityUtils.setLoginUser(loginUser);
                }
            } catch (Exception e) {
                // 令牌无效或过期：不设置登录用户，后续接口按未登录处理
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityUtils.removeLoginUser();
        }
    }
}
