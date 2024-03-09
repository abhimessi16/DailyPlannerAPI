package me.abhilashbhyrava.service;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Planner;
import me.abhilashbhyrava.model.Task;
import me.abhilashbhyrava.model.TimeSlot;
import me.abhilashbhyrava.repository.PlannerRepository;
import me.abhilashbhyrava.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TaskService taskService;
    private final PlannerRepository plannerRepository;

    public TimeSlot addTimeSlot(TimeSlot timeSlot){
        return timeSlotRepository.save(timeSlot);
    }

    public TimeSlot addTimeSlot(int startTime, List<Task> tasks){
        List<Task> addedTasks = taskService.addAllTasks(tasks);
        return  timeSlotRepository
                .save(
                        TimeSlot.builder()
                                .startTime(startTime)
                                .tasks(addedTasks)
                                .build()
                );
    }

    public List<TimeSlot> addAllTimeSlots(List<TimeSlot> timeSlots){
        return timeSlots.stream().map(
                timeSlot -> addTimeSlot(timeSlot.getStartTime(), timeSlot.getTasks())
        ).toList();
    }

    public TimeSlot getTimeSlot(String username, int timeSlot) {
        Optional<Planner> optionalPlanner = plannerRepository.findByUsername(username);

        if(optionalPlanner.isEmpty())
            return null;

        Planner planner = optionalPlanner.orElseThrow();

        return planner.getTimeSlots().get(timeSlot - planner.getStartTime());
    }


    public List<TimeSlot> getAllTimeSlots(String username) {
        Optional<Planner> planner = plannerRepository.findByUsername(username);

        // there's so much to learn
        // i didn't even think of mapping!
        return planner.map(Planner::getTimeSlots).orElse(new ArrayList<>());
    }
}
