package me.abhilashbhyrava.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Planner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;

    // these refer to the time-frame start and end
    // for which the user wants to plan his tasks
    private Integer startTime;
    private Integer endTime;

    @OneToMany
    @Cascade(CascadeType.PERSIST)
    private List<TimeSlot> timeSlots;

}
