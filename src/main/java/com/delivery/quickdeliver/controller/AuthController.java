package com.delivery.quickdeliver.controller;

import com.delivery.quickdeliver.domain.entity.User;
import com.delivery.quickdeliver.dto.request.LoginRequest;
import com.delivery.quickdeliver.dto.request.SignupRequest;
import com.delivery.quickdeliver.dto.response.ApiResponse;
import com.delivery.quickdeliver.dto.response.JwtResponse;
import com.delivery.quickdeliver.exception.DuplicateResourceException;
import com.delivery.quickdeliver.repository.UserRepository;
import com.delivery.quickdeliver.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 및 회원가입 API")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = (User) authentication.getPrincipal();
        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        log.info("User {} logged in successfully", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", jwtResponse));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup attempt for username: {}", signupRequest.getUsername());

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new DuplicateResourceException("이미 사용 중인 사용자명입니다");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다");
        }

        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .phoneNumber(signupRequest.getPhoneNumber())
                .role(signupRequest.getRole())
                .enabled(true)
                .build();

        userRepository.save(user);

        log.info("User {} registered successfully", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다"));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<JwtResponse>> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        JwtResponse userInfo = JwtResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
