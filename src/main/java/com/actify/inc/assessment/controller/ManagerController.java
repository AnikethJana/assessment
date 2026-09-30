package com.actify.inc.assessment.controller;

import com.actify.inc.assessment.dto.ApiResponse;
import com.actify.inc.assessment.dto.TaskCreateRequest;
import com.actify.inc.assessment.dto.TaskResponse;
import com.actify.inc.assessment.dto.UserResponse;
import com.actify.inc.assessment.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final TaskService taskService;

    public ManagerController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersWithTasks() {
        List<UserResponse> users = taskService.getAllUsersWithTasks();
        return ResponseEntity.ok(ApiResponse.success("Users with assigned tasks retrieved successfully", users));
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse taskResponse = taskService.assignTask(request);
        return new ResponseEntity<>(ApiResponse.created("Task assigned successfully", taskResponse), HttpStatus.CREATED);
    }
}
