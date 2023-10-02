package ru.practicum.explore.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations")
@JsonIgnoreProperties({ "id" })
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Локация должна содержать данные о широте")
    private float lat;
    @NotNull(message = "Локация должна содержать данные о широте")
    private float lon;
}
