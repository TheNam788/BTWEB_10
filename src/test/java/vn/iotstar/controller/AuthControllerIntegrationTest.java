package vn.iotstar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vn.iotstar.dto.LoginRequest;
import vn.iotstar.dto.LoginResponse;
import vn.iotstar.dto.RegisterRequest;
import vn.iotstar.entity.User;
import vn.iotstar.service.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Kịch bản 1: Đăng ký người dùng mới thành công qua POST /register")
    void testRegisterSuccess() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("teststudent")
                .email("student@hcmute.edu.vn")
                .password("123456")
                .fullName("Nguyen Van Hoc Vien")
                .role("USER")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@hcmute.edu.vn"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Hoc Vien"));
    }

    @Test
    @DisplayName("Kịch bản 2 & 3: Đăng nhập sinh token Nimbus và truy cập API protected /users/me")
    void testLoginAndAccessProtectedResource() throws Exception {
        // Đăng ký trước
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("jwtuser")
                .email("jwtuser@hcmute.edu.vn")
                .password("Password@123")
                .fullName("JWT Nimbus User")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Đăng nhập
        LoginRequest loginRequest = LoginRequest.builder()
                .email("jwtuser@hcmute.edu.vn")
                .password("Password@123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseJson, LoginResponse.class);
        String token = loginResponse.getToken();

        assertNotNull(token);

        // Truy cập GET /users/me với Header Authorization: Bearer <token>
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jwtuser@hcmute.edu.vn"))
                .andExpect(jsonPath("$.fullName").value("JWT Nimbus User"));
    }

    @Test
    @DisplayName("Kịch bản 4: Gọi API protected không có token bị từ chối 401")
    void testAccessWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Kịch bản 5: Gọi API protected với Token sai chữ ký bị từ chối 401")
    void testAccessWithInvalidTokenReturns401() throws Exception {
        // Chuỗi token giả mạo không hợp lệ
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJmYWtlIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalidSignatureString1234567890";

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Kịch bản 6: Gọi API protected với Token hết hạn bị từ chối 401")
    void testAccessWithExpiredTokenReturns401() throws Exception {
        User user = User.builder()
                .email("expired_user@hcmute.edu.vn")
                .username("expired_user")
                .password("123456")
                .role("USER")
                .enabled(true)
                .build();

        // Tạo token đã hết hạn 10 giây trước
        String expiredToken = jwtService.generateTokenWithCustomExpiration(user, -10000);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
