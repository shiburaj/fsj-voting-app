# Full Stack Voting App
Simple JAVA Servlet + ReactJS app to online voting. Backend in JAVA Servlet and Mysql database. Frontend in ReactJS.

## DB Structure ERD
```
erDiagram
    users ||--o{ sessions : "references"
    sessions ||--o{ candidates : "references"
    sessions ||--o{ votes : "references"
    candidates ||--o{ votes : "references"
    users ||--o{ votes : "references"
    users {
        BIGINT id PK
        VARCHAR name ""
        VARCHAR email ""
        VARCHAR password ""
    }
    sessions {
        BIGINT id PK
        VARCHAR name ""
        BIGINT user_id FK
        BOOLEAN voting_enabled ""
        VARCHAR code ""
    }
    candidates {
        BIGINT id PK
        BIGINT session_id FK
        VARCHAR name ""
    }
    votes {
        BIGINT id PK
        BIGINT session_id FK
        BIGINT candidate_id FK
        BIGINT voter_id FK
    }
```

## The Flow
1. User Registers and Logs IN.
2. Creates a new session and adds candidates names
3. start voting
4. Voters logsin and clicks on cast vote then enters the session code
5. candidates names are shown any one is selected and vote is casted
6. The session creator is able the disable voting session and see the result.
7. Results are updated using polling every 4 secs

## Note
1. Backend
  - Use JAVA Servlet + JDBC + Gson for making the api.
  - Keep it simple as it should be understood by beginers
  - Add CORS filter
  - Use models and DAO 

2. Frontend
  - Use ReactJS and fetch
  - Use shadcn/ui
  - Keep the design simple