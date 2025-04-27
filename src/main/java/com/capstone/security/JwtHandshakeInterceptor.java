package com.capstone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Base64;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final String jwtSecret = "9YzZChTTO4l+XqPeSc3lF2uk6xGu4gq4gFCI1t7r7FU=";

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {

        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.replace("token=", "").split("&")[0];

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret)))
                        .parseClaimsJws(token)
                        .getBody();

                String userEmail = claims.getSubject();
                attributes.put("userEmail", userEmail);

                attributes.put("principal", (java.security.Principal) () -> userEmail);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
