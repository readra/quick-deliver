package com.delivery.quickdeliver.config;

import com.delivery.quickdeliver.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        // /topic: 1:N 브로드캐스트
        // /queue: 1:1 메시징
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 메시지 전송 시 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지 전송 시 사용할 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS fallback 지원

        // SockJS 없는 순수 WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * 클라이언트 인바운드 채널에 JWT 인증 인터셉터를 등록한다.
     * STOMP CONNECT 프레임 수신 시 Authorization 헤더의 JWT를 검증한다.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
