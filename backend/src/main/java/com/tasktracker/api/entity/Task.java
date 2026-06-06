package com.tasktracker.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title must not be blank")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean completed = false;

    // When serializing a Task we include the category object, but suppress
    // its tasks list to avoid infinite recursion.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"tasks", "hibernateLazyInitializer", "handler"})
    private Category category;

    public Task() {}

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getTitle()                 { return title; }
    public void setTitle(String title)       { this.title = title; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public boolean isCompleted()                     { return completed; }
    public void setCompleted(boolean completed)      { this.completed = completed; }

    public Category getCategory()                    { return category; }
    public void setCategory(Category category)       { this.category = category; }
}
