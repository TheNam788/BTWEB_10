package vn.iotstar.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Sinh JWT Token cho UserDetails với thời gian hết hạn mặc định
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Sinh JWT Token có chứa thêm các custom claims (role, user_id, ...)
     * Sử dụng hoàn toàn thư viện Nimbus JOSE + JWT:
     * - JWTClaimsSet.Builder
     * - JWSHeader (HS256)
     * - SignedJWT
     * - MACSigner
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpiration);

            // 1. Tạo JWTClaimsSet
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .jwtID(UUID.randomUUID().toString());

            // Thêm authorities / role vào claims
            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            claimsBuilder.claim("roles", authorities);

            // Bổ sung các claim mở rộng nếu có
            if (extraClaims != null) {
                extraClaims.forEach(claimsBuilder::claim);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // 2. Tạo JWSHeader với thuật toán HS256
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // 3. Khởi tạo SignedJWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // 4. Ký token bằng MACSigner và secret key
            JWSSigner signer = new MACSigner(getSigningKeyBytes());
            signedJWT.sign(signer);

            // 5. Serialize thành chuỗi compact token dạng Header.Payload.Signature
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Lỗi khi sinh JWT bằng Nimbus JOSE: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo JWT Token", e);
        }
    }

    /**
     * Sinh token với thời gian hết hạn tùy biến (phục vụ test token hết hạn)
     */
    public String generateTokenWithCustomExpiration(UserDetails userDetails, long customExpirationMs) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + customExpirationMs);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .jwtID(UUID.randomUUID().toString())
                    .claim("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            JWSSigner signer = new MACSigner(getSigningKeyBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Lỗi khi sinh JWT custom expiration: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo JWT Token", e);
        }
    }

    /**
     * Parse chuỗi token thành SignedJWT
     */
    public SignedJWT parseToken(String token) throws ParseException {
        return SignedJWT.parse(token);
    }

    /**
     * Trích xuất username (subject) từ JWT
     */
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = parseToken(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("Lỗi parse token khi lấy username: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Trích xuất toàn bộ claims từ token
     */
    public JWTClaimsSet extractAllClaims(String token) throws ParseException {
        return parseToken(token).getJWTClaimsSet();
    }

    /**
     * Trích xuất claim cụ thể bằng hàm resolver
     */
    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        try {
            final JWTClaimsSet claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ParseException e) {
            log.error("Lỗi trích xuất claim từ token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy thời gian hết hạn của token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    /**
     * Kiểm tra xem token đã hết hạn chưa
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration == null || expiration.before(new Date());
    }

    /**
     * Kiểm tra tính hợp lệ của token với UserDetails:
     * - Chữ ký phải đúng (MACVerifier)
     * - Token chưa hết hạn
     * - Subject phải khớp với username của UserDetails
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = parseToken(token);

            // Kiểm tra chữ ký bằng MACVerifier
            JWSVerifier verifier = new MACVerifier(getSigningKeyBytes());
            if (!signedJWT.verify(verifier)) {
                log.warn("Chữ ký JWT không hợp lệ (Signature mismatch)");
                return false;
            }

            // Kiểm tra thời gian hết hạn
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                log.warn("Token JWT đã hết hạn (Expired token)");
                return false;
            }

            // Kiểm tra subject khớp username
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            return subject != null && subject.equals(userDetails.getUsername());
        } catch (ParseException | JOSEException e) {
            log.warn("Không thể xác thực JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Xác thực riêng phần chữ ký
     */
    public boolean verifySignature(String token) {
        try {
            SignedJWT signedJWT = parseToken(token);
            JWSVerifier verifier = new MACVerifier(getSigningKeyBytes());
            return signedJWT.verify(verifier);
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    /**
     * Chuyển secretKey thành mảng byte có độ dài tối thiểu 256 bits (32 bytes)
     */
    private byte[] getSigningKeyBytes() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return padded;
        }
        return keyBytes;
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}
