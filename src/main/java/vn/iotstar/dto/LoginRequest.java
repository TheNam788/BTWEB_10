package vn.iotstar.dto;

public class LoginRequest {
    private String email;
    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    public static class LoginRequestBuilder {
        private String email;
        private String username;
        private String password;

        public LoginRequestBuilder email(String email) { this.email = email; return this; }
        public LoginRequestBuilder username(String username) { this.username = username; return this; }
        public LoginRequestBuilder password(String password) { this.password = password; return this; }

        public LoginRequest build() {
            return new LoginRequest(email, username, password);
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLoginIdentifier() {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return "";
    }
}
