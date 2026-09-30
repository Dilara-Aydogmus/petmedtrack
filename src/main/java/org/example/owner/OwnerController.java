package org.example.owner;

import jakarta.validation.Valid;
import org.example.owner.dto.CreateOwnerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService){
        this.ownerService = ownerService;
    }

    @GetMapping
    public List<Owner> getAllOwners(){
        return ownerService.findAll();
    }

    @GetMapping("/{id}")
    public Owner getOwnerById(@PathVariable Long id){
        Owner owner = ownerService.findById(id);
        return owner;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Owner createOwner(@Valid @RequestBody CreateOwnerRequest request){
        Owner owner = ownerService.create(request);
        return owner;
    }

    @PutMapping("/{id}")
    public Owner updateOwner(@PathVariable Long id, @Valid @RequestBody CreateOwnerRequest request){
        Owner updatedOwner = ownerService.update(id, request);
        return updatedOwner;
    }

    @DeleteMapping("/{id}/permanent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void permanentlyDeleteOwner(@PathVariable Long id){
        ownerService.permanentlyDeleteById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwner(@PathVariable Long id){
        ownerService.deactivateById(id);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateOwner(@PathVariable Long id){
        ownerService.activateById(id);
    }
}

