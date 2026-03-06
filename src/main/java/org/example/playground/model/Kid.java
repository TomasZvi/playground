package org.example.playground.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kid {
    @Id
    private String ticketNumber;

    private String name;
    private String age;
    private boolean acceptWaiting;
}
