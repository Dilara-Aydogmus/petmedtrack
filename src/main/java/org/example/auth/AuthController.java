package org.example.auth;
import jakarta.validation.Valid;
import org.example.auth.dto.CreateUserRequest;
import org.example.auth.dto.LoginRequest;
import org.example.auth.dto.UserResponse;
import org.example.security.JwtService;
import org.example.veterinarian.Veterinarian;
import org.example.veterinarian.VeterinarianService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final VeterinarianService veterinarianService;

    public AuthController(UserService userService, JwtService jwtService, VeterinarianService veterinarianService){
        this.userService = userService;
        this.jwtService = jwtService;
        this.veterinarianService = veterinarianService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request){
        String role = request.getRole().trim().toUpperCase();

        if (!role.equals("USER") && !role.equals("VET")) {
            throw new IllegalArgumentException("Role must be USER or VET");
        }

       Veterinarian veterinarian = null;
        if(role.equals("VET")){
            if(request.getVeterinarianId() == null){
                throw new IllegalArgumentException("Veterinarian id is required for VET users");
            }
            veterinarian = veterinarianService.findById(request.getVeterinarianId());
        }
        User user = new User(request.getUsername(),
                        request.getPassword(),
                        role,
                        veterinarian);
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
