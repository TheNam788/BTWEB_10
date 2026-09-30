package vn.iotstar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.dto.LoginRequest;
import vn.iotstar.dto.LoginResponse;
import vn.iotstar.dto.RegisterRequest;
import vn.iotstar.entity.User;
import vn.iotstar.service.AuthenticationService;

@RestController
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Endpoint đăng ký tài khoản (hỗ trợ cả /register và /auth/signup theo bài giảng)
     */
    @PostMapping(path = {"/register", "/auth/signup"})
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest) {
        User registeredUser = authenticationService.register(registerRequest);
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * Endpoint đăng nhập tài khoản sinh JWT (hỗ trợ cả /login và /auth/login theo bài giảng)
     */
    @PostMapping(path = {"/login", "/auth/login"})
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}
