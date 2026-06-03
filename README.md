# Vanilla Java REST API Blog Engine

A lightweight, high-performance RESTful API for a blogging platform built entirely in **pure Java** without using heavy frameworks like Spring.
Designed to demonstrate low-level web-server mechanics, secure database interaction, and clean layered architecture.

## Architecture Overview
The application strictly follows the **Separation of Concerns** principle and is split into distinct architectural layers:
- **Presentation Layer (`HttpServer` / `HttpHandler`):** Low-level socket management, manual URI parsing, and route management.
- **Business Logic Layer (`BlogService`):** Domain validation and core business rules.
- **Data Access Layer (`PostgresSQLRepository`):** Pure JDBC mapping using `PreparedStatement` and secure transaction control.
- **Composition Root (`Main`):** Manual Dependency Injection and Graceful Shutdown handling (resource cleanup via Shutdown Hook).

## Tech Stack
- **Language:** Java 21
- **Database:** PostgreSQL
- **JSON Parser:** Jackson Databind
- **Testing:** JUnit 5, Mockito, Java native `HttpClient`

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/posts` | Retrieve all blog posts |
| `GET` | `/api/posts/{id}` | Get a specific post by ID |
| `GET` | `/api/posts?query={text}` | Wildcard search across content and titles |
| `POST` | `/api/posts` | Create a new blog post |
| `PUT` | `/api/posts/{id}` | Update an existing post |
| `DELETE` | `/api/posts/{id}` | Delete a post by ID |
## Database Schema

The application uses **PostgreSQL** as its relational database management system. The schema consists of four tables managing posts, categories, and tags with proper foreign key constraints and cascading deletes.
```text 
CREATE TABLE categories (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE posts (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    category_id bigint REFERENCES categories(id)
);

CREATE TABLE tags (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE post_tags (
    post_id bigint REFERENCES posts(id) ON DELETE CASCADE,
    tag_id bigint REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);
```

### Entity-Relationship Diagram (ERD)

```text
  [categories] 1 ──── 0..* [posts] 0..* ──── 0..* [tags]
                             │                      │
                             └────► [post_tags] ◄───┘


```


## How to Run

1. Make sure you have **PostgreSQL** running locally and a database created.
2. Set up the required environment variables:
   - `DB_PASSWORD` (Required: your database password)
   - `DB_URL` (Optional: defaults to `jdbc:postgresql://localhost:5432/blog_db`)
   - `DB_USER` (Optional: defaults to `postgres`)
3. Build and run the application via your IDE or terminal:
   ```bash
   ./gradlew build
java -jar build/libs/your-app-name.jar
