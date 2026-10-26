package org.example.auth;

import org.example.clinic.Clinic;
import org.example.common.ResourceNotFoundException;
import org.example.veterinarian.Veterinarian;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.example.owner.Owner;

@Service
public class CurrentUserService {
    private final UserService userService;
    public CurrentUserService(UserService userService){
        this.userService = userService;
    }

    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if(user == null){
            throw new ResourceNotFoundException("Current user not found");
        }
        return user;
    }

    public Clinic getCurrentClinic(){
        User user = getCurrentUser();
        Clinic clinic = user.getClinic();
        if(clinic == null){
            throw new IllegalArgumentException("User is not connected to a clinic");
        }
        return clinic;
        }


    public Long getCurrentClinicId(){
        Clinic clinic = getCurrentClinic();
        return clinic.getId();
    }

    public Owner getCurrentOwner(){
        User user = getCurrentUser();
        String userRole = user.getRole();
        boolean isNormalUser = "USER".equalsIgnoreCase(userRole);
        if(!isNormalUser){
            throw new IllegalArgumentException("Only normal users can access an owner");
        }
        Owner owner = user.getOwner();

        if(owner == null){
            throw new IllegalArgumentException("Current user is not connected to an owner");
        }
        return owner;
    }
}
