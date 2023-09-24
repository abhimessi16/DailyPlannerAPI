package me.abhilashbhyrava.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer startTime;

    // this might change
    // for instance let's say that there is a task
    // which is to be completed in 4 hours then it must span all the four timeslots
    // I suppose that is how it should be
    // , but we're not displaying all tasks available in a timeslot on the card itself
    // like if there are more tasks then we have to hide a few
    // so lets think about this
    // how it should work and all
    // if the above thought is valid then this relation
    // will become many-to-many
    @OneToMany
    @Cascade(CascadeType.PERSIST)
    private List<Task> tasks;

    public Long getFinishCount(){
        return tasks.stream().filter(
                task -> task.getTaskStatus() == TaskStatus.COMPLETED
        ).count();
    }

}
