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

    // TEXT type allows longer strings than VARCHAR - good for descriptions
    @Column(columnDefinition = "TEXT")
    private String description;

    // defaults to false so new tasks are always incomplete
    @Column(nullable = false)
    private boolean completed = false;

    // many tasks can belong to one category (many-to-one relationship)
    // FetchType.LAZY means we don't load the category from DB until we actually access it
    // @JsonIgnoreProperties stops the serializer from going into category.tasks and causing infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"tasks", "hibernateLazyInitializer", "handler"})
    private Category category;

    // JPA requires a no-arg constructor
    public Task() {}

    // standard getters and setters
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
