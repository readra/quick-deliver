package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.dto.response.RouteInfo;
import com.delivery.quickdeliver.service.KakaoRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 길찾기(경로 조회) REST API
 * GET /api/routes
 *   ?originLat=37.xxx&originLng=127.xxx
 *   &destLat=37.yyy&destLng=127.yyy
 *   [&waypointLat=37.zzz&waypointLng=127.zzz]
 */
@Slf4j
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Route", description = "길찾기 / 경로 조회 API")
public class RouteController {

    private final KakaoRouteService kakaoRouteService;

    @Operation(summary = "도로 경로 조회",
               description = "Kakao Mobility API를 통해 실제 도로 경로와 ETA를 반환한다. " +
                             "API 키 미설정 시 available=false를 반환하며 프론트엔드는 직선 fallback을 사용한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<RouteInfo>> getRoute(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destLat,
            @RequestParam double destLng,
            @RequestParam(required = false) Double waypointLat,
            @RequestParam(required = false) Double waypointLng) {

        log.debug("[Route] 경로 조회 요청: ({},{}) → ({},{})", originLat, originLng, destLat, destLng);

        RouteInfo route = kakaoRouteService.getRoute(
                originLat, originLng,
                destLat,   destLng,
                waypointLat, waypointLng
        );

        return ResponseEntity.ok(ApiResponse.success(route));
    }
}
