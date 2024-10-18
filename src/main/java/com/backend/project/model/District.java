package com.backend.project.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document(collection="Districts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class District {
    @Id
    private Integer id;

    private String name;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "district")
    private List<Office> offices = new ArrayList<>();


    public District(Integer id, String name){
        this.id = id;
        this.name = name;
    }
}
