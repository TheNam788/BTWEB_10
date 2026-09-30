package vn.iotstar.dto;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String email, String password, String fullName, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public static RegisterRequestBuilder builder() {
        return new RegisterRequestBuilder();
    }

    public static class RegisterRequestBuilder {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String role;

        public RegisterRequestBuilder username(String username) { this.username = username; return this; }
        public RegisterRequestBuilder email(String email) { this.email = email; return this; }
        public RegisterRequestBuilder password(String password) { this.password = password; return this; }
        public RegisterRequestBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public RegisterRequestBuilder role(String role) { this.role = role; return this; }

        public RegisterRequest build() {
            return new RegisterRequest(username, email, password, fullName, role);
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
