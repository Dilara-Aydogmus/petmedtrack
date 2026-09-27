package org.example.auth;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public User save(User user){
        return userRepository.save(user);
    }
    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }
}
