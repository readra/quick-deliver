package com.delivery.quickdeliver.dto.route;

import lombok.Data;

import java.util.List;

@Data
public class RiderRoute {
    private String riderId;
    private String deliveryId;
    private String routeName;
    private List<Waypoint> waypoints;
    
    // 현재 진행 상태
    private int currentWaypointIndex = 0;
    private long startTime = 0;
    
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.currentWaypointIndex = 0;
    }
    
    public boolean isCompleted() {
        return currentWaypointIndex >= waypoints.size() - 1;
    }
    
    public Waypoint getCurrentWaypoint() {
        if (waypoints == null || waypoints.isEmpty()) {
            return null;
        }
        int index = Math.min(currentWaypointIndex, waypoints.size() - 1);
        return waypoints.get(index);
    }
    
    public void updateProgress() {
        if (isCompleted()) {
            return;
        }
        
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        
        // 경과 시간에 맞는 waypoint 찾기
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).getTimestamp() <= elapsedSeconds) {
                currentWaypointIndex = i;
            } else {
                break;
            }
        }
    }
}
