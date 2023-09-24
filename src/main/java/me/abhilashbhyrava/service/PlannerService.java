package me.abhilashbhyrava.service;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Planner;
import me.abhilashbhyrava.model.TimeSlot;
import me.abhilashbhyrava.repository.PlannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final TimeSlotService timeSlotService;

    public Planner getPlanner(String username) {
        return plannerRepository.findByUsername(username).orElse(null);
    }

    public List<Planner> getPlanners(){
        return plannerRepository.findAll();
    }

    public boolean addPlanner(Planner plannerToAdd) {

        if(plannerRepository
                .findByUsername(plannerToAdd.getUsername())
                .isPresent()
        )
            return false;

        // first we are going to be creating users
        // and, then we will be adding the tasks to the timeslots
        // we won't ask the user to add all tasks right when he is registering
        // so, it doesn't make sense to make the add request have
        // timeslots in it with tasks!

        plannerToAdd.setTimeSlots(new ArrayList<>());

        for(int timeSlot = plannerToAdd.getStartTime(); timeSlot < plannerToAdd.getEndTime(); timeSlot++){

            TimeSlot slot = TimeSlot.builder()
                    .startTime(timeSlot)
                    .tasks(new ArrayList<>())
                    .build();

            plannerToAdd.getTimeSlots().add(slot);
        }

        plannerRepository.save(plannerToAdd);
        return true;
    }

    public boolean removePlanner(String username) {

        Optional<Planner> plannerToRemove = plannerRepository.findByUsername(username);

        if(plannerToRemove.isEmpty())
            return false;

        // from above condition we know that planner exists
        plannerToRemove.ifPresent(plannerRepository::delete);

        return true;
    }

    @Transactional
    public int updatePlanner(String username, Planner updatePlanner) {

        Optional<Planner> plannerToUpdate = plannerRepository.findByUsername(username);

        if(plannerToUpdate.isEmpty())
            return 0;
        else if(!username.equals(updatePlanner.getUsername()))
            return 1;

        // getting the planner to update
        Planner oldPlanner = plannerToUpdate.orElseThrow();

        oldPlanner = updateTimeSlots(
                oldPlanner,
                updatePlanner.getStartTime(),
                updatePlanner.getEndTime()
        );

        plannerRepository.save(oldPlanner);
        return 2;
    }

    public int[] getPlannerStatus(String username) {

        Optional<Planner> plannerToCount = plannerRepository.findByUsername(username);
        if(plannerToCount.isEmpty())
            return new int[] {};

        Planner planner = plannerToCount.orElseThrow();
        int taskCount = 0;
        int taskFinishCount = 0;

        for(TimeSlot timeSlot : planner.getTimeSlots()){
            taskCount += timeSlot.getTasks().size();
            taskFinishCount += timeSlot.getFinishCount();
        }
        return new int[] {taskCount, taskFinishCount};
    }

    public Planner updateTimeSlots(Planner planner, Integer startTime, Integer endTime) {

        // extracting the existing timeslots
        List<TimeSlot> timeSlots = planner.getTimeSlots();

        // emptying the old planner's timeslots
        planner.setTimeSlots(new ArrayList<>());

        /*
            I should add some logic to remove the timeslots
            which are not needed anymore!
            let's do this later
         */

        // for each updated slot time
        for(int slot = startTime; slot < endTime; slot++){

            // check if the time slot existed earlier and add it
            if(slot >= planner.getStartTime() && planner.getEndTime() > slot) {
                planner.getTimeSlots().add(timeSlots.get(slot - planner.getStartTime()));
            }
            // if time slot is new then create an empty timeslot object and add to list
            else{
                TimeSlot timeSlot = timeSlotService.addTimeSlot(slot, new ArrayList<>());
                planner.getTimeSlots().add(timeSlot);
            }
        }

        // setting new start and end times
        planner.setStartTime(startTime);
        planner.setEndTime(endTime);

        return planner;
    }

}
