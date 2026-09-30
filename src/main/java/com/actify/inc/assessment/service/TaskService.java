package com.actify.inc.assessment.service;

import com.actify.inc.assessment.dto.TaskCreateRequest;
import com.actify.inc.assessment.dto.TaskResponse;
import com.actify.inc.assessment.entity.Task;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.repository.TaskRepository;
import com.actify.inc.assessment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse assignTask(TaskCreateRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + request.getUserId());
        }

        Task saved = taskRepository.save(new Task(request.getTitle(), user));

        return new TaskResponse(saved.getId(), saved.getTitle(), user.getId(), user.getName());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUserEmail(String email) {
        List<Task> tasks = taskRepository.findByAssignedUserEmail(email);
        List<TaskResponse> responseList = new ArrayList<>();
        if (tasks != null) {
            for (Task t : tasks) {
                responseList.add(new TaskResponse(t.getId(), t.getTitle(), t.getAssignedUser().getId(), t.getAssignedUser().getName()));
            }
        }
        return responseList;
    }
}
