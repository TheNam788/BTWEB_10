package vn.iotstar.service;

import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByIdentifier(String identifier) {
        return userRepository.findByEmailOrUsername(identifier, identifier);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
