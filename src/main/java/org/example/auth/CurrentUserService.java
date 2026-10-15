package org.example.auth;

import org.example.clinic.Clinic;
import org.example.common.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


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
        if( !"VET".equalsIgnoreCase(user.getRole()) || user.getVeterinarian() == null ){
            throw new IllegalArgumentException("Current user is not connected to a veterinarian");
        }
        return user.getVeterinarian().getClinic();
    }
    public Long getCurrentClinicId(){
        Clinic clinic = getCurrentClinic();
        return clinic.getId();
    }
}
