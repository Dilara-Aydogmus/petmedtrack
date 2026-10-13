package org.example.pet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.owner.Owner;

import java.time.LocalDateTime;
import java.math.BigDecimal;

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

    private Integer ageYears;
    private Integer ageMonths;
    private BigDecimal weight;
    private BigDecimal height;

    @Column(nullable = false)
    private boolean neutered = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    protected Pet(){

    }

    public Pet(String name, String species, String breed, Integer ageYears, Integer ageMonths, BigDecimal weight, BigDecimal height, boolean neutered, Owner owner){
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.ageYears = ageYears;
        this.ageMonths = ageMonths;
        this.weight = weight;
        this.height = height;
        this.neutered = neutered;
        this.owner = owner;
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
    public Integer getAgeYears() {
        return ageYears;
    }
    public Integer getAgeMonths() {
        return ageMonths;
    }
    public String getAgeText(){
        return ageYears + " yıl " + ageMonths + " ay ";
    }


    public BigDecimal getWeight() {
        return weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public boolean isNeutered() {
        return neutered;
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

    public void setAgeYears(Integer ageYears) {
        this.ageYears = ageYears;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public void setNeutered(boolean neutered) {
        this.neutered = neutered;
    }
    public void setOwner(Owner owner){
        this.owner = owner;
    }

    @JsonIgnore
    public Owner getOwner(){
        return owner;
    }
}


