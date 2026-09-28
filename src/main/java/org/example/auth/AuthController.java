package org.example.auth;

import jakarta.validation.Valid;
import org.example.auth.dto.CreateUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.example.auth.dto.LoginRequest;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody CreateUserRequest request){
        User user = new User(request.getUsername(), request.getPassword(), request.getRole());
        return userService.save(user);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request){
        boolean isValid = userService.login(request.getUsername(), request.getPassword());
        if(!isValid){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        return "Login successful";
    }

}
