package com.actify.inc.assessment.service;

import com.actify.inc.assessment.dto.*;
import com.actify.inc.assessment.entity.Role;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.exception.DuplicateResourceException;
import com.actify.inc.assessment.exception.ResourceNotFoundException;
import com.actify.inc.assessment.repository.RoleRepository;
import com.actify.inc.assessment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists.");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                roles.add(resolveRole(roleName));
            }
        } else {
            roles.add(resolveRole("ROLE_USER"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String newEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("User with email '" + newEmail + "' already exists.");
        }

        user.setName(request.getName());
        user.setEmail(newEmail);

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
                throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                roles.add(resolveRole(roleName));
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public UserResponse assignRolesToUser(Long id, AssignRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            roles.add(resolveRole(roleName));
        }
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    public Role resolveRole(String roleName) {
        String trimmed = roleName.trim();
        String standardName = trimmed.toUpperCase().startsWith("ROLE_")
                ? trimmed.toUpperCase()
                : "ROLE_" + trimmed.toUpperCase();

        return roleRepository.findByName(standardName)
                .or(() -> roleRepository.findByName(trimmed))
                .orElseGet(() -> roleRepository.save(new Role(standardName)));
    }

    public UserResponse mapToUserResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        List<TaskResponse> tasks = user.getTasks() != null ? user.getTasks().stream()
                .map(t -> new TaskResponse(t.getId(), t.getTitle(), user.getId(), user.getName()))
                .collect(Collectors.toList()) : List.of();

        return new UserResponse(user.getId(), user.getName(), user.getEmail(), roles, tasks);
    }
}
