package com.actify.inc.assessment.service;

import com.actify.inc.assessment.dto.TaskCreateRequest;
import com.actify.inc.assessment.dto.TaskResponse;
import com.actify.inc.assessment.dto.UserResponse;
import com.actify.inc.assessment.entity.Task;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.exception.ResourceNotFoundException;
import com.actify.inc.assessment.repository.TaskRepository;
import com.actify.inc.assessment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       UserService userService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public TaskResponse assignTask(TaskCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Task task = new Task(request.getTitle(), user);
        Task savedTask = taskRepository.save(task);

        return new TaskResponse(savedTask.getId(), savedTask.getTitle(), user.getId(), user.getName());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersWithTasks() {
        return userService.getAllUsers();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUserEmail(String email) {
        return taskRepository.findByAssignedUserEmail(email).stream()
                .map(t -> new TaskResponse(t.getId(), t.getTitle(), t.getAssignedUser().getId(), t.getAssignedUser().getName()))
                .collect(Collectors.toList());
    }
}
