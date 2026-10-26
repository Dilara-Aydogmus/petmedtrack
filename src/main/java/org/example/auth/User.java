package org.example.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.veterinarian.Veterinarian;
import org.example.clinic.Clinic;
import org.example.owner.Owner;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinarian_id")
    private Veterinarian veterinarian;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    public User(String username, String password, String role, Veterinarian veterinarian,
                String firstName, String lastName, String email, String phoneNumber,
                Clinic clinic, Owner owner) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.veterinarian = veterinarian;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.clinic = clinic;
        this.owner = owner;
    }
}
