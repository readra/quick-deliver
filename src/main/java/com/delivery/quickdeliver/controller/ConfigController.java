package com.delivery.quickdeliver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/config")
@Tag(name = "Config API", description = "프론트엔드 설정 제공 API")
public class ConfigController {

    @Value("${kakao.map.api-key}")
    private String kakaoMapApiKey;

    @GetMapping("/map")
    @Operation(summary = "지도 설정 조회", description = "Kakao Map API 키 등 지도 설정을 조회합니다.")
    public ResponseEntity<Map<String, String>> getMapConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("provider", "kakao");
        config.put("apiKey", kakaoMapApiKey);
        
        return ResponseEntity.ok(config);
    }
}
