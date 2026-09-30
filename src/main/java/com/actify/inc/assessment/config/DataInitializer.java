package com.actify.inc.assessment.config;

import com.actify.inc.assessment.entity.Role;
import com.actify.inc.assessment.entity.Task;
import com.actify.inc.assessment.entity.User;
import com.actify.inc.assessment.repository.RoleRepository;
import com.actify.inc.assessment.repository.TaskRepository;
import com.actify.inc.assessment.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           TaskRepository taskRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            // Seed Roles
            Role adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
            Role managerRole = roleRepository.save(new Role("ROLE_MANAGER"));
            Role userRole = roleRepository.save(new Role("ROLE_USER"));

            // Seed Admin User
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);

            // Seed Manager User
            User manager = new User();
            manager.setName("Project Manager");
            manager.setEmail("manager@example.com");
            manager.setPassword(passwordEncoder.encode("Manager@123"));
            manager.setRoles(Set.of(managerRole));
            userRepository.save(manager);

            // Seed Regular User
            User regularUser = new User();
            regularUser.setName("John Doe");
            regularUser.setEmail("user@example.com");
            regularUser.setPassword(passwordEncoder.encode("User@123"));
            regularUser.setRoles(Set.of(userRole));
            User savedRegularUser = userRepository.save(regularUser);

            // Seed Multi-Role User (Manager + User)
            User teamLead = new User();
            teamLead.setName("Alice Lead");
            teamLead.setEmail("lead@example.com");
            teamLead.setPassword(passwordEncoder.encode("Lead@123"));
            Set<Role> multiRoles = new HashSet<>();
            multiRoles.add(managerRole);
            multiRoles.add(userRole);
            teamLead.setRoles(multiRoles);
            User savedTeamLead = userRepository.save(teamLead);

            // Seed Sample Tasks
            taskRepository.save(new Task("Complete unit test coverage", savedRegularUser));
            taskRepository.save(new Task("Review pull request #42", savedRegularUser));
            taskRepository.save(new Task("Prepare sprint retrospective", savedTeamLead));
        }
    }
}
