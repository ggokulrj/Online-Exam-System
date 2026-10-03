# Online Exam System (Spring Boot + MySQL + JWT)

## Run
1. Install Java 17+ and MySQL. Open this folder in IntelliJ as a Maven project.
2. Edit `src/main/resources/application.properties`: set your MySQL password, a new JWT secret, and the admin password.
3. Run `OnlineExamApplication`. Tables are created automatically; an ADMIN user is created on first start.

## API (send the token as `Authorization: Bearer <token>`)
| Who | Method & URL | Purpose |
|---|---|---|
| Public | POST /api/auth/register | `{"username","password"}` creates a STUDENT |
| Public | POST /api/auth/login | returns a JWT token |
| Admin | POST /api/admin/exams | `{"title","description","durationMinutes","passPercentage"}` |
| Admin | POST /api/admin/exams/{id}/questions | `{"text","optionA".."optionD","correctOption":"B"}` |
| Admin | GET /api/admin/exams/{id}/report | average, highest, lowest, pass/fail counts |
| Student | GET /api/exams | list exams |
| Student | POST /api/exams/{id}/start | begins the attempt, returns questions (no answers) |
| Student | POST /api/attempts/{attemptId}/submit | `{"answers":{"1":"B","2":"D"}}` returns the result |
| Student | GET /api/attempts/my | personal performance report |

## Security tests (Step 5)
- No token -> 401. Student token on /api/admin/** -> 403.
- Submit the same attempt twice -> 409. Start the same exam twice -> 409.
- Submit someone else's attempt id -> 403. Submit after the time limit -> score 0.
- Register with `"role":"ADMIN"` in the body -> ignored, you still become a STUDENT.
- Wrong password -> 401 with the same message as an unknown user.
