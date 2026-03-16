package com.delivery.quickdeliver.service;

import com.delivery.quickdeliver.dto.response.RouteInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kakao Mobility Directions v1 API를 호출해 실제 도로 경로와 ETA를 조회하는 서비스.
 *
 * <p>API 키가 설정되지 않거나 호출에 실패하면 {@code available = false}인 {@link RouteInfo}를
 * 반환한다. 프론트엔드는 이 경우 기존 직선 점선 경로(fallback)를 유지한다.</p>
 *
 * <p>Kakao Mobility API 좌표 형식은 <b>경도(lng), 위도(lat)</b> 순서다.</p>
 */
@Slf4j
@Service
public class KakaoRouteService {

    private static final String KAKAO_MOBILITY_URL =
            "https://apis-navi.kakaomobility.com/v1/directions";

    @Value("${kakao.mobility.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 출발지 → (경유지) → 목적지 경로를 조회한다.
     *
     * @param originLat    출발지 위도
     * @param originLng    출발지 경도
     * @param destLat      목적지 위도
     * @param destLng      목적지 경도
     * @param waypointLat  경유지 위도 (null 가능)
     * @param waypointLng  경유지 경도 (null 가능)
     * @return 경로 정보 (available=false 이면 API 미설정 또는 실패)
     */
    public RouteInfo getRoute(double originLat, double originLng,
                              double destLat, double destLng,
                              Double waypointLat, Double waypointLng) {

        if (apiKey == null || apiKey.isBlank()) {
            log.debug("[KakaoRoute] Mobility API 키 미설정 → fallback 반환");
            return unavailable();
        }

        try {
            // Kakao Mobility API는 경도(lng),위도(lat) 순서
            String origin      = originLng + "," + originLat;
            String destination = destLng   + "," + destLat;

            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(KAKAO_MOBILITY_URL)
                    .queryParam("origin",      origin)
                    .queryParam("destination", destination)
                    .queryParam("priority",    "RECOMMEND");

            if (waypointLat != null && waypointLng != null) {
                uriBuilder.queryParam("waypoints", waypointLng + "," + waypointLat);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    uriBuilder.build(true).toUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return parseResponse(response.getBody());

        } catch (Exception e) {
            log.warn("[KakaoRoute] API 호출 실패: {}", e.getMessage());
            return unavailable();
        }
    }

    // ── 내부 파싱 헬퍼 ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private RouteInfo parseResponse(Map<String, Object> body) {
        if (body == null) return unavailable();

        List<Map<String, Object>> routes =
                (List<Map<String, Object>>) body.get("routes");
        if (routes == null || routes.isEmpty()) return unavailable();

        Map<String, Object> route = routes.get(0);
        int resultCode = ((Number) route.get("result_code")).intValue();
        if (resultCode != 0) {
            log.warn("[KakaoRoute] 경로 없음 result_code={}", resultCode);
            return unavailable();
        }

        // 요약 정보
        Map<String, Object> summary = (Map<String, Object>) route.get("summary");
        int duration = ((Number) summary.get("duration")).intValue(); // 초
        int distance = ((Number) summary.get("distance")).intValue(); // 미터

        // vertexes에서 좌표 추출: sections → roads → vertexes (lng, lat, lng, lat, ...)
        List<double[]> path = new ArrayList<>();
        List<Map<String, Object>> sections =
                (List<Map<String, Object>>) route.get("sections");

        if (sections != null) {
            for (Map<String, Object> section : sections) {
                List<Map<String, Object>> roads =
                        (List<Map<String, Object>>) section.get("roads");
                if (roads == null) continue;

                for (Map<String, Object> road : roads) {
                    List<Number> vertexes = (List<Number>) road.get("vertexes");
                    if (vertexes == null) continue;

                    // vertexes는 [lng1, lat1, lng2, lat2, ...] 형식
                    for (int i = 0; i + 1 < vertexes.size(); i += 2) {
                        double lng = vertexes.get(i).doubleValue();
                        double lat = vertexes.get(i + 1).doubleValue();
                        path.add(new double[]{lat, lng}); // Kakao Maps: lat 먼저
                    }
                }
            }
        }

        return RouteInfo.builder()
                .available(true)
                .path(path)
                .durationSeconds(duration)
                .distanceMeters(distance)
                .etaFormatted(formatDuration(duration))
                .build();
    }

    private RouteInfo unavailable() {
        return RouteInfo.builder().available(false).build();
    }

    /** 초 단위 소요 시간을 "약 N분" 또는 "약 N시간 M분" 형식으로 변환 */
    private String formatDuration(int seconds) {
        if (seconds < 60) return "1분 미만";
        int minutes = seconds / 60;
        if (minutes < 60) return "약 " + minutes + "분";
        int hours   = minutes / 60;
        int rem     = minutes % 60;
        return rem > 0
                ? "약 " + hours + "시간 " + rem + "분"
                : "약 " + hours + "시간";
    }
}
