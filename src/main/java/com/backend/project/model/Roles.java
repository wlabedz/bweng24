package com.backend.project.model;


import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("roles")
@Getter
@Setter
public class Roles {

    @Id
    private UUID id;

    private String name;

    public Roles(String name){
        this.id = UUID.randomUUID();
        this.name = name;
    }
}
