package com.delivery.quickdeliver.dto.response;

import com.delivery.quickdeliver.domain.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String name;
    private String email;
    private UserRole role;
    private String riderId;  // 라이더 ID 추가

    public JwtResponse(String token, Long id, String username, String name, String email, UserRole role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
