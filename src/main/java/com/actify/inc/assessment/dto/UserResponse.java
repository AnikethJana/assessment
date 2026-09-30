package com.actify.inc.assessment.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Set<String> roles = new HashSet<>();
    private List<TaskResponse> tasks = new ArrayList<>();

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, Set<String> roles, List<TaskResponse> tasks) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.roles = roles != null ? roles : new HashSet<>();
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public List<TaskResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskResponse> tasks) {
        this.tasks = tasks;
    }
}
