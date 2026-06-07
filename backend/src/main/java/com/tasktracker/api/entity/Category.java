package com.tasktracker.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

// @Entity tells JPA/Hibernate that this class maps to a database table
@Entity
@Table(name = "category")
public class Category {

    // auto-incremented primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the name must not be blank and must be unique across all categories
    @NotBlank(message = "Category name must not be blank")
    @Column(nullable = false, unique = true)
    private String name;

    // one category can have many tasks - cascade ALL means if we delete a category, its tasks get deleted too
    // @JsonIgnoreProperties prevents infinite recursion when serializing to JSON
    // (Task has a category field, Category has a tasks field - without this they'd loop forever)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("category")
    private List<Task> tasks = new ArrayList<>();

    // JPA requires a no-arg constructor
    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    // standard getters and setters
    public Long getId()               { return id; }
    public void setId(Long id)        { this.id = id; }

    public String getName()           { return name; }
    public void setName(String name)  { this.name = name; }

    public List<Task> getTasks()              { return tasks; }
    public void setTasks(List<Task> tasks)    { this.tasks = tasks; }
}
