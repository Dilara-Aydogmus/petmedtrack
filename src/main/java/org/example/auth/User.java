package org.example.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.veterinarian.Veterinarian;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinarian_id")
    private Veterinarian veterinarian;

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    public User(String username, String password, String role, Veterinarian veterinarian){
        this.username = username;
        this.password = password;
        this.role = role;
        this.veterinarian = veterinarian;
    }
}
