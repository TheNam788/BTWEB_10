package vn.iotstar.dto;

public class LoginResponse {
    private String token;
    private long expiresIn;
    private String tokenType = "Bearer";

    public LoginResponse() {
    }

    public LoginResponse(String token, long expiresIn, String tokenType) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.tokenType = (tokenType != null) ? tokenType : "Bearer";
    }

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String token;
        private long expiresIn;
        private String tokenType = "Bearer";

        public LoginResponseBuilder token(String token) { this.token = token; return this; }
        public LoginResponseBuilder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public LoginResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }

        public LoginResponse build() {
            return new LoginResponse(token, expiresIn, tokenType);
        }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
