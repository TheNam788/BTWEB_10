package vn.iotstar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iotstar.dto.LoginRequest;
import vn.iotstar.dto.LoginResponse;
import vn.iotstar.dto.RegisterRequest;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Đăng ký tài khoản người dùng mới
     */
    public User register(RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String username = request.getUsername() != null ? request.getUsername().trim() : null;

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng: " + email);
        }
        if (username != null && !username.isBlank() && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username đã tồn tại: " + username);
        }

        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole().toUpperCase() 
                : "USER";

        User user = User.builder()
                .email(email)
                .username(username != null && !username.isBlank() ? username : email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    /**
     * Xác thực thông tin đăng nhập và sinh JWT bằng Nimbus JOSE + JWT
     */
    public LoginResponse authenticate(LoginRequest request) {
        String identifier = request.getLoginIdentifier();
        if (identifier.isBlank()) {
            throw new BadCredentialsException("Email hoặc Username không được để trống");
        }

        // 1. Xác thực username/email và password qua Spring Security AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        // 2. Tìm User trong Database
        User user = userRepository.findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Người dùng không tồn tại"));

        // 3. Chuẩn bị custom claims cho Nimbus JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("role", user.getRole());
        if (user.getFullName() != null) {
            extraClaims.put("fullName", user.getFullName());
        }

        // 4. Sinh JWT token bằng Nimbus JOSE + JWT
        String token = jwtService.generateToken(extraClaims, user);

        // 5. Trả về LoginResponse
        return LoginResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationTime())
                .tokenType("Bearer")
                .build();
    }

    /**
     * Xác thực và trả về Entity User (tương thích method trong slide bài giảng)
     */
    public User authenticateUser(LoginRequest request) {
        String identifier = request.getLoginIdentifier();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );
        return userRepository.findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Người dùng không tồn tại"));
    }
}
