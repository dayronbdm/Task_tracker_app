Internet Technologies and Web Services final project 

Task Tracker App

A full-stack web application for managing tasks and categories.
Built with Spring Boot (backend) and Vue 3 (frontend).

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.2
- Spring Data JPA
- H2 In-Memory Database

**Frontend**
- Vue 3 

## Features

- View, create, edit, and delete **Categories**
- View, create, edit, and delete **Tasks**
- Assign tasks to a category
- Toggle tasks as completed / pending
- Filter tasks by All, Active, or Completed
- Cascading delete — removing a category removes its tasks
- Seed data loaded automatically on startup
- Responsive two-column dashboard layout

## Database

Two tables with a One-to-Many relationship:

- `category` — id, name
- `task` — id, title, description, completed, category_id (FK)


## How to Run

bash run.sh
