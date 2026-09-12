package org.example.pet;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    private String breed;

    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Pet(){

    }

    public Pet(String name, String species, String breed, LocalDate birthDate){
          this.name = name;
          this.species = species;
          this.breed = breed;
          this.birthDate = birthDate;
          this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getSpecies() {
        return species;
    }
    public String getBreed() {
        return breed;
    }
    public LocalDate getBirthDate() {
        return birthDate;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setSpecies(String species){
        this.species = species;
    }
    public void setBreed(String breed){
        this.breed = breed;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}


