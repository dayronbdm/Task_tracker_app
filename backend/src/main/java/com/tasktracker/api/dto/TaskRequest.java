package com.tasktracker.api.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskRequest {

    @NotBlank(message = "Task title must not be blank")
    private String title;

    private String description;

    private boolean completed = false;

    private Long categoryId;

    public String getTitle()                 { return title; }
    public void setTitle(String title)       { this.title = title; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public boolean isCompleted()                     { return completed; }
    public void setCompleted(boolean completed)      { this.completed = completed; }

    public Long getCategoryId()                      { return categoryId; }
    public void setCategoryId(Long categoryId)       { this.categoryId = categoryId; }
}
