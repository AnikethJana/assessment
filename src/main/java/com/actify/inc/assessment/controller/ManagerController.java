package com.actify.inc.assessment.controller;

import com.actify.inc.assessment.dto.TaskCreateRequest;
import com.actify.inc.assessment.dto.TaskResponse;
import com.actify.inc.assessment.dto.UserResponse;
import com.actify.inc.assessment.service.TaskService;
import com.actify.inc.assessment.service.UserService;
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
    private final UserService userService;

    public ManagerController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsersWithTasks() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> assignTask(@Valid @RequestBody TaskCreateRequest request) {
        return new ResponseEntity<>(taskService.assignTask(request), HttpStatus.CREATED);
    }
}
