package com.backend.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private UUID id;

    private String name;

    private String surname;

    private String username;

    private String mail;

    private String password;

    @ManyToMany(fetch= FetchType.EAGER, cascade= CascadeType.ALL)
    @JoinTable(name="user_roles", joinColumns = @JoinColumn(name= "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name="role_id", referencedColumnName = "id"))
    private List<Roles> roles = new ArrayList<>();

    public UserEntity(String name, String surname, String mail, String username, String password){
        this.id = UUID.randomUUID();
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.username = username;
        this.password = password;
    }
}
