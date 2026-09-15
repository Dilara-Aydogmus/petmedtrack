package org.example.owner;
import org.example.common.ResourceNotFoundException;
import org.example.owner.dto.CreateOwnerRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }
    public List<Owner> findAll() {
        List<Owner> owners = ownerRepository.findAll();
        return owners;
    }

    public Owner findById(Long id){
        Optional<Owner> ownerOptional = ownerRepository.findById(id);

        if(ownerOptional.isEmpty()) {
            String errorMessage = "Owner not found:" + id;
            throw new ResourceNotFoundException(errorMessage);
        }

        Owner owner = ownerOptional.get();
        return owner;
    }

    public Owner create(CreateOwnerRequest request){
        Owner owner = new Owner(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPhoneNumber());
        Owner savedOwner = save(owner);
        return savedOwner;
    }

    public Owner update(Long id, CreateOwnerRequest request){
        Owner owner = findById(id);
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setEmail(request.getEmail());
        owner.setPhoneNumber(request.getPhoneNumber());
        Owner updatedOwner = save(owner);
        return updatedOwner;
    }
    public Owner save(Owner owner){
        Owner savedOwner = ownerRepository.save(owner);
        return savedOwner;

    }

    public void deleteById(Long id) {
        ownerRepository.deleteById(id);
    }

}
