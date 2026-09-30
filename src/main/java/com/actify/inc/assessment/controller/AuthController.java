package com.actify.inc.assessment.controller;

import com.actify.inc.assessment.dto.ApiResponse;
import com.actify.inc.assessment.dto.AuthRequest;
import com.actify.inc.assessment.dto.AuthResponse;
import com.actify.inc.assessment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }
}
