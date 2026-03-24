# MicroQuest Starter

MicroQuest is a full-stack Spring Boot starter app for a fun challenge-sharing website. Users can create short “micro-adventures”, browse ideas, save favorites, and leave comments.

## Why this app works well as a portfolio project

- It is entertaining and useful.
- It naturally grows to 7–10 pages.
- It uses a real relational database that can scale far beyond a toy demo.
- It gives you room to add authentication, search, notifications, badges, APIs, and analytics later.

## Included pages

1. Home
2. About
3. Browse quests
4. Quest detail
5. Create / edit quest
6. Users list
7. User profile
8. Create profile
9. Leaderboard

## Tech stack

- Java 17
- Spring Boot
- Spring MVC
- Thymeleaf
- Spring Data JPA
- PostgreSQL
- Maven
- Docker + Docker Compose
- Bootstrap via CDN
- Visual Studio Code friendly project layout

## Development notes

This project is intentionally kept in development mode:

- PostgreSQL uses `ddl-auto=update` for convenience.
- Thymeleaf template caching is disabled.
- Sample seed data is inserted automatically when the database is empty.
- No paid services are required.

## Run locally with Docker for PostgreSQL + Maven for the app

### 1. Start the database

```bash
docker compose up db -d
```

### 2. Run the Spring Boot app from VS Code terminal

```bash
mvn spring-boot:run
```

The site will be available at:

```text
http://localhost:8080
```

## Run the whole app with Docker Compose

```bash
docker compose up --build
```

## Default database settings

- Database: `microquest`
- Username: `microquest`
- Password: `microquest`
- Port: `5432`

You can override them with environment variables.

## Good next features to add

- Spring Security login / registration
- Search by tag or city
- Image uploads for quests
- Badges and streaks
- REST API endpoints
- Pagination for comments
- Email notifications
- Admin moderation
- Leaderboard filters by week / month / all time
