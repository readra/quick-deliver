package com.delivery.quickdeliver.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Kakao Mobility 길찾기 API 응답을 가공한 경로 정보 DTO
 */
@Getter
@Builder
public class RouteInfo {

    /** API 호출 성공 여부 (false 면 프론트엔드는 fallback 직선 경로를 사용) */
    private boolean available;

    /** 경로 좌표 배열 [[lat, lng], ...] — Kakao Maps LatLng 순서 */
    private List<double[]> path;

    /** 예상 소요 시간 */
    private int durationSeconds;

    /** 총 거리 */
    private int distanceMeters;

    /** 포맷된 ETA 문자열 (예: "약 23분", "약 1시간 5분") */
    private String etaFormatted;
}
