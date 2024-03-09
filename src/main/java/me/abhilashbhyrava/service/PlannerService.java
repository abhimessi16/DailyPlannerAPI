package me.abhilashbhyrava.service;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Planner;
import me.abhilashbhyrava.model.Task;
import me.abhilashbhyrava.model.TimeSlot;
import me.abhilashbhyrava.repository.PlannerRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final TimeSlotService timeSlotService;
    private final TaskService taskService;

    public Planner getPlanner(String username) {
        return plannerRepository.findByUsername(username).orElse(null);
    }

    public List<Planner> getPlanners(){
        return plannerRepository.findAll();
    }

    public List<String> getPlannerNames(){
        return getPlanners().stream().map(Planner::getUsername).collect(Collectors.toList());
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

        for (int timeSlot = plannerToAdd.getStartTime(); timeSlot < plannerToAdd.getEndTime(); timeSlot++) {

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

        System.out.println(oldPlanner);

        oldPlanner = updateTimeSlots(
                oldPlanner,
                updatePlanner.getStartTime(),
                updatePlanner.getEndTime()
        );

        System.out.println(oldPlanner);

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

    public void syncWithGoogleCalendar(String accessToken, String username) {
        // first check if current user has logged in using google account
//        if(SecurityContextHolder.getContext().getAuthentication())


        List<TimeSlot> timeSlots = timeSlotService.getAllTimeSlots(username);

        var calendarService = new GoogleCalendarService(accessToken);

        for(TimeSlot timeSlot : timeSlots){
            System.out.println(timeSlot.getId());

            List<Task> addTasks = calendarService.syncTasksToAdd(timeSlot);

            addTasks.forEach(task -> timeSlot.getTasks().add(taskService.addTask(task)));
            timeSlotService.addTimeSlot(timeSlot);

            System.out.println(timeSlot);

            Set<Integer> removeTasks = calendarService.syncTasksToRemove(timeSlot);
            List<Task> syncedTasks = new ArrayList<>();

//            timeSlot.setTasks(syncedTasks);
            for(Task task : timeSlot.getTasks()){
                if(!removeTasks.contains(task.getTaskId()))
                    syncedTasks.add(task);
            }
            timeSlot.setTasks(syncedTasks);
            timeSlotService.addTimeSlot(timeSlot);
        }

    }
}
