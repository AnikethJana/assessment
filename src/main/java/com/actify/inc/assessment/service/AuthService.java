package com.actify.inc.assessment.service;

import com.actify.inc.assessment.dto.AuthRequest;
import com.actify.inc.assessment.dto.AuthResponse;
import com.actify.inc.assessment.entity.Role;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.repository.UserRepository;
import com.actify.inc.assessment.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Set<String> roles = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                roles.add(role.name());
            }
        }

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), roles);
    }
}
