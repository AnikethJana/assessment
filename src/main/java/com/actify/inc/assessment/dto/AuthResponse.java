package com.actify.inc.assessment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";  // always Bearer, set once as field default
    private Long userId;
    private String name;
    private String email;
    private Set<String> roles;

    public AuthResponse(String token, Long userId, String name, String email, Set<String> roles) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.roles = roles;
        // tokenType stays "Bearer" from field default
    }
}
