package vn.iotstar.exception;

import com.nimbusds.jose.JOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Sai thông tin đăng nhập", ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Từ chối truy cập", "Bạn không có quyền truy cập tài nguyên này");
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<Map<String, Object>> handleAccountStatus(AccountStatusException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Tài khoản bị khóa", "Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa");
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Map<String, Object>> handleParseException(ParseException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "JWT không hợp lệ", "Cấu trúc token JWT không đúng định dạng");
    }

    @ExceptionHandler(JOSEException.class)
    public ResponseEntity<Map<String, Object>> handleJoseException(JOSEException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "JWT sai signature", "Chữ ký số của JWT không hợp lệ");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi trên hệ thống";
        if (msg.toLowerCase().contains("expired") || msg.toLowerCase().contains("hết hạn")) {
            return buildResponse(HttpStatus.UNAUTHORIZED, "JWT đã hết hạn", "Token JWT đã hết hạn sử dụng");
        }
        if (msg.toLowerCase().contains("signature") || msg.toLowerCase().contains("chữ ký")) {
            return buildResponse(HttpStatus.UNAUTHORIZED, "JWT sai signature", "Chữ ký số JWT không hợp lệ");
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi máy chủ nội bộ", msg);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
