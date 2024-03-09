package me.abhilashbhyrava.model;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskId;

    private String taskName;
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

}
