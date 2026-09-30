package com.actify.inc.assessment.dto;

public class TaskResponse {

    private Long id;
    private String title;
    private Long userId;
    private String userName;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, Long userId, String userName) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.userName = userName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
