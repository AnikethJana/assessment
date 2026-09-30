package com.actify.inc.assessment.service;

import com.actify.inc.assessment.dto.*;
import com.actify.inc.assessment.entity.Role;
import com.actify.inc.assessment.entity.Task;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email '" + request.getEmail() + "' already exists.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
        }
        user.setRoles(roles);

        return mapToUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : users) {
            userResponses.add(mapToUserResponse(user));
        }
        return userResponses;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return mapToUserResponse(findUserById(id));
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserById(id);

        String newEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("User with email '" + newEmail + "' already exists.");
        }

        user.setName(request.getName());
        user.setEmail(newEmail);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(request.getRoles());
        }

        return mapToUserResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.delete(findUserById(id));
    }

    public UserResponse assignRolesToUser(Long id, AssignRolesRequest request) {
        User user = findUserById(id);
        user.setRoles(request.getRoles());
        return mapToUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return mapToUserResponse(user);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private User findUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user;
    }

    public UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                roleNames.add(role.name());
            }
        }

        List<TaskResponse> taskResponses = new ArrayList<>();
        if (user.getTasks() != null) {
            for (Task task : user.getTasks()) {
                taskResponses.add(new TaskResponse(task.getId(), task.getTitle(), user.getId(), user.getName()));
            }
        }

        return new UserResponse(user.getId(), user.getName(), user.getEmail(), roleNames, taskResponses);
    }
}
