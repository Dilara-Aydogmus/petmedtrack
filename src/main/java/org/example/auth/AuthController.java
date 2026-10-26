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
import org.example.auth.dto.CurrentUserResponse;
import org.example.clinic.Clinic;
import org.example.clinic.ClinicService;
import org.example.owner.Owner;
import org.example.owner.OwnerService;
import org.example.owner.dto.CreateOwnerRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final VeterinarianService veterinarianService;
    private final ClinicService clinicService;
    private final CurrentUserService currentUserService;
    private final OwnerService ownerService;

    public AuthController(UserService userService, JwtService jwtService,
                          VeterinarianService veterinarianService, ClinicService clinicService,
                          CurrentUserService currentUserService, OwnerService ownerService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.veterinarianService = veterinarianService;
        this.clinicService = clinicService;
        this.currentUserService = currentUserService;
        this.ownerService = ownerService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        String role = request.getRole().trim().toUpperCase();

        if (!role.equals("USER") && !role.equals("VET")) {
            throw new IllegalArgumentException("Role must be USER or VET");
        }

        Veterinarian veterinarian = null;
        Owner owner = null;
        Clinic userClinic;

        if (role.equals("VET")) {
            if (request.getClinicName() == null || request.getClinicName().isBlank()
                    || request.getClinicPhoneNumber() == null || request.getClinicPhoneNumber().isBlank()) {
                throw new IllegalArgumentException("Clinic name and phone number are required for VET users");
            }

            userClinic = clinicService.create(
                    request.getClinicName().trim(),
                    request.getClinicAddress(),
                    request.getClinicPhoneNumber().trim()
            );

            veterinarian = veterinarianService.create(
                    request.getFirstName().trim(),
                    request.getLastName().trim(),
                    request.getEmail().trim(),
                    request.getPhoneNumber().trim(),
                    userClinic.getId()
            );
        } else {
            if (request.getClinicName() == null || request.getClinicName().isBlank()) {
                throw new IllegalArgumentException("Clinic name is required for USER users");
            }

            userClinic = clinicService.findByName(request.getClinicName().trim());
            if (userClinic == null) {
                throw new IllegalArgumentException("Clinic not found");
            }

            CreateOwnerRequest ownerRequest = new CreateOwnerRequest();
            ownerRequest.setFirstName(request.getFirstName().trim());
            ownerRequest.setLastName(request.getLastName().trim());
            ownerRequest.setEmail(request.getEmail().trim());
            ownerRequest.setPhoneNumber(request.getPhoneNumber().trim());
            ownerRequest.setClinicId(userClinic.getId());

            owner = ownerService.createForUserRegistration(ownerRequest, userClinic);
        }

        User user = new User(
                request.getUsername(),
                request.getPassword(),
                role,
                veterinarian,
                request.getFirstName().trim(),
                request.getLastName().trim(),
                request.getEmail().trim(),
                request.getPhoneNumber().trim(),
                userClinic,
                owner
        );

        User savedUser = userService.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        boolean isValid = userService.login(request.getUsername(), request.getPassword());
        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        return jwtService.generateToken(request.getUsername());
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser() {
        User user = currentUserService.getCurrentUser();
        Clinic clinic = user.getClinic();

        Long clinicId = null;
        String clinicName = null;

        if (clinic != null) {
            clinicId = clinic.getId();
            clinicName = clinic.getName();
        }

        Veterinarian veterinarian = user.getVeterinarian();
        if (veterinarian == null) {
            return new CurrentUserResponse(user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    null,
                    null,
                    clinicId,
                    clinicName);

        }
            return new CurrentUserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    veterinarian.getId(),
                    veterinarian.getFirstName() + " " + veterinarian.getLastName(),
                    clinicId,
                    clinicName
            );

        }

}