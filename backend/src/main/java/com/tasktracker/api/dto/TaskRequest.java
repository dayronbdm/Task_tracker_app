package com.tasktracker.api.dto;

import jakarta.validation.constraints.NotBlank;

// DTO = Data Transfer Object
// This class is what the frontend sends in the request body when creating or updating a task
// We use a separate DTO instead of the Task entity directly because we don't want to expose
// the full entity structure to the outside world
public class TaskRequest {

    // title is required - @NotBlank checks for null and empty/whitespace strings
    @NotBlank(message = "Task title must not be blank")
    private String title;

    // description is optional
    private String description;

    // defaults to false so tasks start as incomplete
    private boolean completed = false;

    // the ID of the category to assign - can be null if no category is selected
    private Long categoryId;

    // getters and setters (Spring uses these to read the JSON request body)
    public String getTitle()                 { return title; }
    public void setTitle(String title)       { this.title = title; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public boolean isCompleted()                     { return completed; }
    public void setCompleted(boolean completed)      { this.completed = completed; }

    public Long getCategoryId()                      { return categoryId; }
    public void setCategoryId(Long categoryId)       { this.categoryId = categoryId; }
}
