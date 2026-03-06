package org.example.playground.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaySite {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<AttractionConfiguration> attractions = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Kid> kidsOnSite = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Kid> kidsQueue = new ArrayList<>();
}
