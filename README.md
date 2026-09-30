# HỆ THỐNG XÁC THỰC SPRING BOOT VỚI NIMBUS JOSE + JWT

Dự án bài tập môn **Lập trình Web (WEBPR330479)** - Trường Đại học Sư Phạm Kỹ Thuật TP.HCM (HCMUTE).  
Ứng dụng hoàn thành yêu cầu xây dựng kiến trúc xác thực Stateless JWT trên nền tảng Spring Boot 3 và Spring Security 6, **thay thế hoàn toàn thư viện JJWT mặc định bằng thư viện chuẩn Nimbus JOSE + JWT**.

---

## 1. Công nghệ sử dụng

- **Java**: 17+ (tương thích hoàn toàn với JDK 17, 21, 26)
- **Framework**: Spring Boot 3.3.0
- **Bảo mật**: Spring Security 6 (Stateless Session, DaoAuthenticationProvider, BCryptPasswordEncoder)
- **Thư viện JWT**: **Nimbus JOSE + JWT (v9.40)** - Tuân thủ các tiêu chuẩn RFC 7515 (JWS), RFC 7519 (JWT)
- **Cơ sở dữ liệu**: MySQL 8.x (với Hibernate / Spring Data JPA)
- **Build Tool**: Apache Maven 3.9+
- **Không sử dụng**: `io.jsonwebtoken` (JJWT), `Thymeleaf`. 100% RESTful API.

---

## 2. Cấu trúc Project

```text
D:\HCMUTE\LT_WEB\CuuBan/
├── database/
│   └── jwt_nimbus.sql                     # Script tạo cơ sở dữ liệu và dữ liệu mẫu
├── src/
│   ├── main/
│   │   ├── java/vn/iotstar/
│   │   │   ├── Application.java            # Class khởi chạy Spring Boot
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java     # Cấu hình Spring Security 6 & Filter Chain
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java     # Endpoints: POST /register, POST /login
│   │   │   │   └── UserController.java     # Endpoints: GET /users/me, GET /users
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java       # DTO nhận thông tin đăng nhập
│   │   │   │   ├── LoginResponse.java      # DTO trả về Token và hạn sử dụng
│   │   │   │   └── RegisterRequest.java    # DTO nhận thông tin đăng ký
│   │   │   ├── entity/
│   │   │   │   └── User.java               # Entity User implements UserDetails
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java # Bắt lỗi chữ ký, hết hạn token (401, 403)
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java     # Truy vấn cơ sở dữ liệu
│   │   │   ├── security/
│   │   │   │   └── JwtAuthenticationFilter.java # OncePerRequestFilter xác thực Bearer token
│   │   │   └── service/
│   │   │       ├── AuthenticationService.java # Nghiệp vụ đăng ký và xác thực đăng nhập
│   │   │       ├── JwtService.java         # Tạo và kiểm tra JWT bằng Nimbus JOSE + JWT
│   │   │       └── UserService.java        # Nghiệp vụ quản lý thông tin User
│   │   └── resources/
│   │       └── application.properties      # Cấu hình kết nối MySQL và Secret Key
│   └── test/
│       ├── java/vn/iotstar/
│       │   ├── controller/
│       │   │   └── AuthControllerIntegrationTest.java # Test 6 kịch bản xác thực
│       │   └── service/
│       │       └── JwtServiceTest.java     # Unit test chuyên sâu Nimbus JOSE + JWT
│       └── resources/
│           └── application.properties      # Cấu hình môi trường test tự động
├── pom.xml                                 # Khai báo dependency Maven
└── README.md                               # Hướng dẫn chi tiết
```

---

## 3. Hướng dẫn thiết lập cơ sở dữ liệu MySQL

### Bước 1: Tạo Database và bảng `users`
Mở **MySQL Workbench**, **DBeaver** hoặc cửa sổ dòng lệnh MySQL Client (`mysql.exe`), chạy toàn bộ nội dung file [database/jwt_nimbus.sql](file:///D:/HCMUTE/LT_WEB/CuuBan/database/jwt_nimbus.sql):

```sql
CREATE DATABASE IF NOT EXISTS `jwt_nimbus` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `jwt_nimbus`;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) UNIQUE NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(150) NULL,
    `role` VARCHAR(50) NOT NULL DEFAULT 'USER',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `images` VARCHAR(500) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dữ liệu mẫu (Mật khẩu mặc định: 123456)
INSERT INTO `users` (`username`, `email`, `password`, `full_name`, `role`, `enabled`)
VALUES 
('admin', 'admin@gmail.com', '$2a$10$3/2pW7k7aJmQoJ1f8rE78.h0YQW2z7E4J7qH0vY1E8V5s1l0G6Y5K', 'Administrator', 'ADMIN', 1),
('user', 'user@gmail.com', '$2a$10$3/2pW7k7aJmQoJ1f8rE78.h0YQW2z7E4J7qH0vY1E8V5s1l0G6Y5K', 'Nguyen Van A', 'USER', 1);
```

### Bước 2: Cấu hình kết nối MySQL trong `application.properties`
Mở file `src/main/resources/application.properties`, kiểm tra và chỉnh sửa tài khoản/mật khẩu MySQL cho đúng với máy tính của bạn:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jwt_nimbus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

---

## 4. Hướng dẫn chạy ứng dụng

### Cách 1: Chạy bằng Maven Command Line
Mở terminal tại thư mục gốc của project:
```bash
mvn clean package
mvn spring-boot:run
```
Ứng dụng sẽ khởi chạy tại cổng: `http://localhost:8005`.

### Cách 2: Chạy trên IntelliJ IDEA / Eclipse / VS Code
- Mở thư mục project trong IDE.
- Đợi IDE import Maven dependencies.
- Tìm file `src/main/java/vn/iotstar/Application.java` và chọn **Run**.

---

## 5. Hướng dẫn Test API bằng Postman / cURL

### 1. Đăng ký tài khoản mới (POST /register hoặc /auth/signup)
- **URL**: `http://localhost:8005/register`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`
- **Body** (Raw JSON):
```json
{
  "username": "sinhvien",
  "email": "sinhvien@hcmute.edu.vn",
  "password": "Password@123",
  "fullName": "Nguyen Van B",
  "role": "USER"
}
```
- **Response thành công** (`200 OK`):
```json
{
  "id": 3,
  "username": "sinhvien",
  "email": "sinhvien@hcmute.edu.vn",
  "fullName": "Nguyen Van B",
  "role": "USER",
  "enabled": true
}
```

---

### 2. Đăng nhập lấy JWT Token (POST /login hoặc /auth/login)
- **URL**: `http://localhost:8005/login`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`
- **Body** (Raw JSON):
```json
{
  "email": "sinhvien@hcmute.edu.vn",
  "password": "Password@123"
}
```
*(Có thể đăng nhập bằng tài khoản mẫu có sẵn: `admin@gmail.com` / `123456`)*
- **Response thành công** (`200 OK`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaW5odmllbkBoY211dGUuZWR1LnZuIiwiZXhwIjoxNz...",
  "expiresIn": 3600000,
  "tokenType": "Bearer"
}
```

---

### 3. Truy cập API Protected: Lấy thông tin cá nhân (GET /users/me)
- **URL**: `http://localhost:8005/users/me`
- **Method**: `GET`
- **Headers**:
  - `Authorization`: `Bearer <token_copy_tu_buoc_2>`
- **Response thành công** (`200 OK`):
```json
{
  "id": 3,
  "username": "sinhvien",
  "email": "sinhvien@hcmute.edu.vn",
  "fullName": "Nguyen Van B",
  "role": "USER",
  "enabled": true,
  "authorities": [
    {
      "authority": "ROLE_USER"
    }
  ]
}
```

---

### 4. Truy cập API Protected: Lấy danh sách Users (GET /users)
- **URL**: `http://localhost:8005/users`
- **Method**: `GET`
- **Headers**:
  - `Authorization`: `Bearer <token>`
- **Response thành công** (`200 OK`): Danh sách tất cả tài khoản người dùng trong hệ thống.

---

### 5. Các trường hợp lỗi bảo mật (Security Tests)
1. **Không truyền Token vào request**:
   - Gửi `GET /users/me` không có header `Authorization`.
   - **Kết quả**: HTTP `401 Unauthorized`.
2. **Token sai chữ ký hoặc bị chỉnh sửa**:
   - Gửi `GET /users/me` với token giả mạo `Bearer eyJhbGciOi...invalid`.
   - **Kết quả**: HTTP `401 Unauthorized` (`JWT sai signature`).
3. **Token đã hết hạn**:
   - Gửi `GET /users/me` với token đã quá hạn.
   - **Kết quả**: HTTP `401 Unauthorized` (`JWT đã hết hạn`).

---

## 6. Điểm khác biệt quan trọng: Nimbus JOSE + JWT vs JJWT

| Tiêu chí | Thư viện cũ: JJWT (`io.jsonwebtoken`) | Thư viện mới: Nimbus JOSE + JWT (`com.nimbusds`) |
| :--- | :--- | :--- |
| **Chuẩn tuân thủ** | Wrapper tùy biến riêng của nhóm JJWT | Tuân thủ tuyệt đối các tiêu chuẩn quốc tế IETF: RFC 7515 (JWS), RFC 7516 (JWE), RFC 7517 (JWK), RFC 7519 (JWT). |
| **Tạo Claims** | `Jwts.builder().claim(...)` | `JWTClaimsSet.Builder().subject(...).claim(...).build()` |
| **Khai báo Header** | Khai báo ngầm hoặc qua chuỗi | Đối tượng minh bạch: `new JWSHeader(JWSAlgorithm.HS256)` |
| **Thực thể JWT** | Chuỗi compact String được build trực tiếp | `SignedJWT signedJWT = new SignedJWT(header, claimsSet)` |
| **Ký số (Signing)** | `Jwts.builder().signWith(key)` | `JWSSigner signer = new MACSigner(secretKey); signedJWT.sign(signer);` |
| **Verify chữ ký** | `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | `SignedJWT.parse(token); JWSVerifier verifier = new MACVerifier(secretKey); signedJWT.verify(verifier)` |
| **Độ ổn định & Tích hợp** | Thay đổi API lớn giữa các phiên bản (0.11.x sang 0.12.x) | Cấu trúc ổn định, được chọn làm thư viện lõi cho Spring Security OAuth2 / Resource Server. |
