# Chat App

Ung dung chat thoi gian thuc duoc xay dung trong thoi gian COVID-19 lockdown. Du an nay giup minh hoc them ve WebSocket, Spring Boot va React trong khi o nha tranh dich.

## Tinh nang

- Nhan tin thoi gian thuc voi WebSocket + STOMP
- Tao va tham gia nhieu phong chat
- Tin nhan rieng (private messaging) giua 2 nguoi dung
- Typing indicator - hien thi khi nguoi khac dang go
- Ho tro emoji va dinh dang tin nhan (bold, italic, code)
- Trang thai online/offline cua nguoi dung
- Dang ky va dang nhap voi Spring Security
- Giao dien React hien dai

## Cong nghe su dung

| Thanh phan | Cong nghe |
|---|---|
| Backend | Java 11, Spring Boot 2.3.4 |
| WebSocket | Spring WebSocket + STOMP + SockJS |
| Database | PostgreSQL 13 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Frontend (v1) | Thymeleaf + Vanilla JS |
| Frontend (v2) | React 17 + React Router |
| HTTP Client | Axios |
| Containerization | Docker + Docker Compose |

## Yeu cau

- Java 11+
- Maven 3.6+
- PostgreSQL 13+
- Node.js 14+ (cho React frontend)
- Docker & Docker Compose (tuy chon)

## Cai dat va chay

### Backend

1. Clone repository:
```bash
git clone https://github.com/daoninhthai/chat-app.git
cd chat-app
```

2. Tao database PostgreSQL:
```sql
CREATE DATABASE chatapp;
```

3. Cau hinh database trong `src/main/resources/application.properties` hoac su dung bien moi truong:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=chatapp
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

4. Chay backend:
```bash
mvn spring-boot:run
```

Backend se chay tai: http://localhost:8080

### Frontend (React)

1. Di chuyen vao thu muc frontend:
```bash
cd frontend
```

2. Cai dat dependencies:
```bash
npm install
```

3. Chay development server:
```bash
npm start
```

Frontend se chay tai: http://localhost:3000

### Docker Quickstart

Cach nhanh nhat de chay toan bo ung dung:

```bash
# Chay tat ca services (app + postgres)
docker-compose up -d

# Hoac chi chay postgres cho local development
docker-compose -f docker-compose.dev.yml up -d
```

Ung dung se co tai: http://localhost:8080

## Cau truc du an

```
chat-app/
├── src/main/java/com/daoninhthai/chatapp/
│   ├── config/          # Cau hinh WebSocket, Security
│   ├── controller/      # REST & WebSocket controllers
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA entities
│   ├── repository/      # Spring Data repositories
│   └── service/         # Business logic layer
├── src/main/resources/
│   ├── static/          # CSS, JS (Thymeleaf frontend)
│   └── templates/       # Thymeleaf templates
├── frontend/            # React frontend
│   ├── src/
│   │   ├── components/  # React components
│   │   ├── context/     # Auth context
│   │   └── services/    # API & WebSocket services
│   └── package.json
├── Dockerfile
├── docker-compose.yml
└── docker-compose.dev.yml
```

## Screenshots

*(Se cap nhat sau)*

## Tac gia

**daoninhthai** - [GitHub](https://github.com/daoninhthai)

Du an nay duoc xay dung de hoc tap va thuc hanh trong thoi gian gian cach xa hoi 2020-2021.
