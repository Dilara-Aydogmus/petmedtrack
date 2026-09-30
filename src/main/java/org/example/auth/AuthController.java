package org.example.auth;

import jakarta.validation.Valid;
import org.example.auth.dto.CreateUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.example.auth.dto.LoginRequest;
import org.springframework.web.server.ResponseStatusException;

import org.example.auth.dto.UserResponse;

import org.example.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService){
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request){
        String role = request.getRole().trim().toUpperCase();

        if (!role.equals("USER") && !role.equals("VET")) {
            throw new IllegalArgumentException("Role must be USER or VET");
        }

        User user = new User(request.getUsername(), request.getPassword(), role);
        User savedUser = userService.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request){
        boolean isValid = userService.login(request.getUsername(), request.getPassword());
        if(!isValid){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        return jwtService.generateToken(request.getUsername());
    }

}
