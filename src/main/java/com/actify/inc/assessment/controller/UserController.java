package com.actify.inc.assessment.controller;

import com.actify.inc.assessment.dto.ApiResponse;
import com.actify.inc.assessment.dto.TaskResponse;
import com.actify.inc.assessment.dto.UserResponse;
import com.actify.inc.assessment.service.TaskService;
import com.actify.inc.assessment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TaskService taskService;

    public UserController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserResponse profile = userService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAssignedTasks(Authentication authentication) {
        String email = authentication.getName();
        List<TaskResponse> tasks = taskService.getTasksByUserEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Assigned tasks retrieved successfully", tasks));
    }
}
