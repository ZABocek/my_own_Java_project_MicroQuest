# MicroQuest

MicroQuest is a full-stack Spring Boot web app for a fun challenge-sharing platform. Users register, browse micro-adventure quests, submit GIF completions with captions, and compete on a leaderboard. Admins can manage quests, users, and appeals.

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring MVC + Spring Security
- Thymeleaf
- Spring Data JPA
- PostgreSQL 17
- Maven
- Bootstrap 5 via CDN

## Prerequisites

Before running the app, make sure the following are installed on your machine:

| Requirement | Version | Notes |
|---|---|---|
| JDK 21 | 21+ | Set `JAVA_HOME` to the JDK 21 directory |
| Maven | 3.9+ | Must be on `PATH` |
| PostgreSQL | 17 | Running as a local Windows service (`postgresql-x64-17`) |

### One-time database setup

Open a terminal and run:

```sql
psql -U postgres
CREATE DATABASE microquest;
CREATE USER microquest WITH PASSWORD 'microquest';
GRANT ALL PRIVILEGES ON DATABASE microquest TO microquest;
\q
```

The app uses `ddl-auto=update`, so tables are created automatically on first launch. Sample seed data is inserted when the database is empty.

## Starting the app (Windows — simplest method)

From the project root, double-click **`start.bat`** or run in a terminal:

```bat
start.bat
```

Or from PowerShell:

```powershell
.\start.ps1
```

The launcher will:
1. Set `JAVA_HOME` and add PostgreSQL `bin` to `PATH`
2. Verify the PostgreSQL service is running (starts it if stopped)
3. Run `mvn spring-boot:run`
4. Open Chrome automatically once the app is ready at `http://localhost:8080`

## Starting the app manually

If you prefer to manage things yourself:

```powershell
# 1. Set Java 21 (adjust path to match your installation)
$env:JAVA_HOME = "C:\jdk21\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

# 2. Start PostgreSQL (if not already running)
Start-Service postgresql-x64-17

# 3. Run the app
mvn spring-boot:run
```

Then open `http://localhost:8080` in your browser.

## Database settings

Configured in `src/main/resources/application.properties`:

| Setting | Value |
|---|---|
| Database | `microquest` |
| Username | `microquest` |
| Password | `microquest` |
| Host | `localhost` |
| Port | `5432` |

## Features

- User registration and login with Spring Security
- Quest browsing, creation, and detail pages
- Multi-GIF submission with per-GIF captions (up to 50 MB per request)
- Private GIF access — only the submitting user (or admin) can view their files
- GIF deletion by the owner
- Leaderboard
- Admin dashboard: manage users, quests, reports, and appeals
- Seed data loaded automatically on first run

## Database Schema (ERD)

```mermaid
erDiagram
    user_profiles {
        bigint id PK
        varchar username "UNIQUE NOT NULL, max 40"
        varchar displayName "NOT NULL, max 80"
        varchar email "UNIQUE, max 254"
        varchar passwordHash "max 60"
        varchar role "NOT NULL: ROLE_USER | ROLE_ADMIN"
        boolean active "NOT NULL, default true"
        timestamp bannedUntil
        int banCount "NOT NULL, default 0"
        boolean emailVerified "NOT NULL, default false"
        varchar emailVerificationToken "max 100"
        varchar photoIdPath "max 500"
        varchar homeCity "max 120"
        varchar bio "max 600"
        timestamp createdAt "NOT NULL"
    }

    quests {
        bigint id PK
        varchar title "NOT NULL, max 120"
        varchar summary "NOT NULL, max 220"
        varchar description "NOT NULL, max 2000"
        varchar category "NOT NULL: FOOD|FITNESS|OUTDOORS|CREATIVE|SOCIAL|LEARNING|RELAXATION|CITY_EXPLORATION"
        varchar difficulty "NOT NULL: EASY | MEDIUM | HARD"
        int estimatedMinutes "NOT NULL"
        boolean indoor "NOT NULL"
        varchar tags "max 200"
        varchar status "NOT NULL: PENDING_APPROVAL | APPROVED | REJECTED"
        timestamp createdAt "NOT NULL"
        timestamp updatedAt "NOT NULL"
        bigint author_id FK
    }

    quest_submissions {
        bigint id PK
        bigint quest_id FK
        bigint user_id FK
        varchar gifPath "NOT NULL, max 500"
        varchar caption "max 500"
        timestamp submittedAt "NOT NULL"
    }

    comments {
        bigint id PK
        varchar body "NOT NULL, max 800"
        timestamp createdAt "NOT NULL"
        bigint quest_id FK
        bigint author_id FK
    }

    quest_saves {
        bigint id PK
        timestamp createdAt "NOT NULL"
        bigint user_id FK
        bigint quest_id FK
    }

    ban_records {
        bigint id PK
        bigint user_id FK
        varchar tier "NOT NULL: ONE_MONTH | THREE_MONTHS | PERMANENT"
        varchar reason "NOT NULL, max 1000"
        timestamp bannedAt "NOT NULL"
        timestamp expiresAt "nullable — null means permanent"
        varchar appealStatus "NOT NULL: PENDING | ACCEPTED | REJECTED"
    }

    appeals {
        bigint id PK
        bigint ban_record_id FK "UNIQUE (1:1 with ban_records)"
        bigint user_id FK
        varchar message "NOT NULL, max 2000"
        varchar status "NOT NULL: PENDING | ACCEPTED | REJECTED"
        varchar adminResponse "max 1000"
        timestamp submittedAt "NOT NULL"
        timestamp reviewedAt
    }

    user_reports {
        bigint id PK
        bigint reported_user_id FK
        bigint reporting_user_id FK
        varchar reason "NOT NULL, max 1000"
        timestamp reportedAt "NOT NULL"
        boolean reviewed "NOT NULL, default false"
    }

    user_profiles ||--o{ quests : "authors"
    user_profiles ||--o{ quest_submissions : "submits"
    user_profiles ||--o{ comments : "writes"
    user_profiles ||--o{ quest_saves : "saves"
    user_profiles ||--o{ ban_records : "banned via"
    user_profiles ||--o{ appeals : "files"
    user_profiles ||--o{ user_reports : "is reported by"
    user_profiles ||--o{ user_reports : "reports"
    quests ||--o{ quest_submissions : "receives"
    quests ||--o{ comments : "has"
    quests ||--o{ quest_saves : "bookmarked via"
    ban_records ||--o| appeals : "appealed via"
```

> **Unique constraint:** `quest_saves(user_id, quest_id)` — a user can bookmark each quest only once.
>
> **Enum values** are stored as strings in PostgreSQL (`EnumType.STRING`).

## Development notes

- `spring.jpa.hibernate.ddl-auto=update` — schema is kept in sync automatically
- `spring.thymeleaf.cache=false` — template changes take effect without restart
- GIF files are stored locally in the `uploads/gifs/` directory (gitignored)
