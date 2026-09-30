package vn.iotstar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 100)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String role = "USER";

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String images;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public User() {
    }

    public User(Integer id, String username, String email, String password, String fullName, String role, Boolean enabled, String images, Date createdAt, Date updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = (role != null) ? role : "USER";
        this.enabled = (enabled != null) ? enabled : true;
        this.images = images;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder Pattern
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Integer id;
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String role = "USER";
        private Boolean enabled = true;
        private String images;
        private Date createdAt;
        private Date updatedAt;

        public UserBuilder id(Integer id) { this.id = id; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder role(String role) { this.role = role; return this; }
        public UserBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public UserBuilder images(String images) { this.images = images; return this; }
        public UserBuilder createdAt(Date createdAt) { this.createdAt = createdAt; return this; }
        public UserBuilder updatedAt(Date updatedAt) { this.updatedAt = updatedAt; return this; }

        public User build() {
            return new User(id, username, email, password, fullName, role, enabled, images, createdAt, updatedAt);
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.isBlank()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(formattedRole));
    }

    @Override
    public String getUsername() {
        return (email != null && !email.isBlank()) ? email : username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
