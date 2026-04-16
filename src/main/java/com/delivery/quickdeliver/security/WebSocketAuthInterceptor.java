package com.delivery.quickdeliver.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * STOMP 연결 시 JWT 토큰을 검증하는 WebSocket 인증 인터셉터.
 *
 * <p>클라이언트는 STOMP CONNECT 프레임의 헤더에 아래와 같이 토큰을 포함해야 한다.
 * <pre>
 *   Authorization: Bearer {jwt_token}
 * </pre>
 *
 * <p>유효한 토큰이면 인증된 {@link java.security.Principal}을 세션에 등록하여
 * 이후 {@code @MessageMapping} 핸들러에서 {@code Principal} 파라미터로 접근할 수 있게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // CONNECT 프레임에서만 인증 검사
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[WS Auth] Authorization 헤더 없음 — 연결 거부");
                throw new MessageDeliveryException("WebSocket 인증 실패: Authorization 헤더 필요");
            }

            String token = authHeader.substring(7).trim();

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[WS Auth] 유효하지 않은 JWT 토큰 — 연결 거부");
                throw new MessageDeliveryException("WebSocket 인증 실패: 유효하지 않은 토큰");
            }

            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 인증 정보를 세션에 등록 및 @MessageMapping에서 Principal 접근 가능
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList()));

            log.debug("[WS Auth] 연결 인증 성공: {}", username);
        }

        return message;
    }
}
