package com.sem2025.g07.gateway.filter;

import com.sem2025.g07.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 白名单路径,无需登录即可访问
    private static final List<String> WHITELIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/doc.html"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单放行
        for (String allowPath : WHITELIST) {
            if (pathMatcher.match(allowPath, path)) {
                return chain.filter(exchange);
            }
        }

        // 获取Authorization Header
        String token = request.getHeaders().getFirst("Authorization");

        // 校验Token格式与有效性
        if (token == null || !token.startsWith("Bearer ")) {
            return unauthorized(exchange, "未携带 Token 或格式错误");
        }

        // 去掉"Bearer "前缀
        token = token.substring(7);

        try {
            // 利用common模块的工具类解析Token
            Claims claims = JwtUtil.parseToken(token);
            
            String userId = claims.getSubject();
            String tenantId = (String) claims.get("tenant_id");

            if (userId == null || tenantId == null) {
                return unauthorized(exchange, "Token 载荷缺失关键信息");
            }

            // 透传Header
            // 将解析出的ID放入Header，传递给User/File Service
            // User/File Service通过UserContext即可获取，无需再次解析Token
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Tenant-Id", tenantId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Token 校验失败: {}", e.getMessage());
            return unauthorized(exchange, "Token 无效或已过期");
        }
    }

    // 返回401未授权响应
    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        // 构造JSON错误提示
        String json = String.format("{\"code\": 401, \"msg\": \"%s\", \"data\": null}", msg);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    // 返回403禁止访问响应
    private Mono<Void> forbidden(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        // 构造JSON错误提示
        String json = String.format("{\"code\": 403, \"msg\": \"%s\", \"data\": null}", msg);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 优先级较高，尽早执行
        return -100;
    }
}