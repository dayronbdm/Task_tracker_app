package com.tasktracker.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name must not be blank")
    @Column(nullable = false, unique = true)
    private String name;

    // When serializing a Category we include its tasks, but suppress the
    // back-reference on each Task to avoid infinite recursion.
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("category")
    private List<Task> tasks = new ArrayList<>();

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    public Long getId()               { return id; }
    public void setId(Long id)        { this.id = id; }

    public String getName()           { return name; }
    public void setName(String name)  { this.name = name; }

    public List<Task> getTasks()              { return tasks; }
    public void setTasks(List<Task> tasks)    { this.tasks = tasks; }
}
