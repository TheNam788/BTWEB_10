package vn.iotstar.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";
    private final long expirationTime = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expirationTime);
    }

    @Test
    @DisplayName("1. Sinh token thành công bằng Nimbus JOSE + JWT")
    void testGenerateToken() throws Exception {
        UserDetails user = new User("testuser@gmail.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        // Parse bằng Nimbus SignedJWT để kiểm tra cấu trúc
        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals(JWSAlgorithm.HS256, signedJWT.getHeader().getAlgorithm());
        assertEquals("testuser@gmail.com", signedJWT.getJWTClaimsSet().getSubject());
        assertNotNull(signedJWT.getJWTClaimsSet().getExpirationTime());
    }

    @Test
    @DisplayName("2. Xác thực token hợp lệ bằng MACVerifier")
    void testValidToken() {
        UserDetails user = new User("valid@gmail.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals("valid@gmail.com", jwtService.extractUsername(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("3. Token sai chữ ký phải bị từ chối")
    void testInvalidSignature() throws Exception {
        // Tạo token với secret key khác hoàn toàn
        String fakeKey = "0000000000000000000000000000000000000000000000000000000000000000";
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("tampered@gmail.com")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60000))
                .build();
        SignedJWT fakeJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        fakeJwt.sign(new MACSigner(fakeKey.getBytes(StandardCharsets.UTF_8)));
        String fakeToken = fakeJwt.serialize();

        UserDetails user = new User("tampered@gmail.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertFalse(jwtService.verifySignature(fakeToken), "Chữ ký sai phải trả về false");
        assertFalse(jwtService.isTokenValid(fakeToken, user), "Token sai chữ ký không được chấp nhận");
    }

    @Test
    @DisplayName("4. Token hết hạn phải bị từ chối")
    void testExpiredToken() {
        UserDetails user = new User("expired@gmail.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        // Tạo token đã hết hạn 5 giây trước
        String expiredToken = jwtService.generateTokenWithCustomExpiration(user, -5000);

        assertTrue(jwtService.isTokenExpired(expiredToken), "Token phải được nhận diện là đã hết hạn");
        assertFalse(jwtService.isTokenValid(expiredToken, user), "Token hết hạn không được hợp lệ");
    }
}
