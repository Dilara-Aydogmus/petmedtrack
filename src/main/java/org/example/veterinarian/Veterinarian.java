package org.example.veterinarian;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.clinic.Clinic;

import java.time.LocalDateTime;

@Entity
@Table(name = "veterinarians")
@Getter
@Setter
@NoArgsConstructor
public class Veterinarian {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Veterinarian(String firstName, String lastName, String email, String phoneNumber, Clinic clinic){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.clinic = clinic;
        this.createdAt = LocalDateTime.now();
    }
}
