package com.actify.inc.assessment.config;

import com.actify.inc.assessment.entity.Role;
import com.actify.inc.assessment.entity.Task;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.repository.TaskRepository;
import com.actify.inc.assessment.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TaskRepository taskRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin       = seed("System Admin",    "admin@example.com",   "Admin@123",   Set.of(Role.ROLE_ADMIN));
            User manager     = seed("Project Manager", "manager@example.com", "Manager@123", Set.of(Role.ROLE_MANAGER));
            User regularUser = seed("John Doe",        "user@example.com",    "User@123",    Set.of(Role.ROLE_USER));
            User teamLead    = seed("Alice Lead",      "lead@example.com",    "Lead@123",    Set.of(Role.ROLE_MANAGER, Role.ROLE_USER));

            taskRepository.save(new Task("Complete unit test coverage", regularUser));
            taskRepository.save(new Task("Review pull request #42", regularUser));
            taskRepository.save(new Task("Prepare sprint retrospective", teamLead));
        }
    }

    private User seed(String name, String email, String password, Set<Role> roles) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
