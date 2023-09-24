package me.abhilashbhyrava.service;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Planner;
import me.abhilashbhyrava.model.Task;
import me.abhilashbhyrava.model.TimeSlot;
import me.abhilashbhyrava.repository.PlannerRepository;
import me.abhilashbhyrava.repository.TaskRepository;
import me.abhilashbhyrava.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PlannerRepository plannerRepository;

    public List<Task> addAllTasks(List<Task> tasks) {
        return tasks.stream().map(this::addTask)
                .collect(Collectors.toList());
    }

    public Task addTask(Task task){
        return taskRepository.save(task);
    }

    public Task getTask(int id) {
        return taskRepository.findById(id).orElse(null);
    }

    public List<Task> getAllTasks(String username, int timeSlot) {

        Planner planner = plannerRepository.findByUsername(username).orElse(null);

        return planner != null
                ? planner.getTimeSlots().get(timeSlot - planner.getStartTime()).getTasks()
                : new ArrayList<>();

    }

    public Task addTask(String username, int timeSlot, Task task) {
        Planner planner = plannerRepository.findByUsername(username).orElse(null);

        if(planner == null)
            return null;

        TimeSlot slot = planner.getTimeSlots().get(timeSlot - planner.getStartTime());
        Task taskToAdd = taskRepository.save(task);
        slot.getTasks().add(taskToAdd);
        timeSlotRepository.save(slot);
        return taskToAdd;
    }

    public boolean deleteTask(String username, int timeSlot, int id) {
        Task taskToDelete = taskRepository.findById(id).orElse(null);
        if(taskToDelete == null)
            return false;

        // if task is present then it is part of a timeslot
        // and the timeslot is part of a planner
        // unless timeslot and/or planner are removed!
        // in which case we will never come across the task
        // in the frontend

        Planner planner = plannerRepository.findByUsername(username).orElse(null);
        if(planner == null)
            return false;

        TimeSlot slot = planner.getTimeSlots().get(timeSlot - planner.getStartTime());
        boolean taskRemoved = slot.getTasks().remove(taskToDelete);
        taskRepository.delete(taskToDelete);
        timeSlotRepository.save(slot);
        return taskRemoved;
    }

    public boolean updateTask(String username, int timeSlot, int id, Task task) {

        Task taskToUpdate = taskRepository.findById(id).orElse(null);
        if(taskToUpdate == null)
            return false;

        Planner planner = plannerRepository.findByUsername(username).orElse(null);
        if(planner == null)
            return false;

        TimeSlot slot = planner.getTimeSlots().get(timeSlot - planner.getStartTime());

        slot.getTasks().remove(taskToUpdate);

        taskToUpdate.setTaskName(task.getTaskName());
        taskToUpdate.setTaskDescription(task.getTaskDescription());
        taskToUpdate.setTaskStatus(task.getTaskStatus());

        Task updatedTask = taskRepository.save(taskToUpdate);

        slot.getTasks().add(updatedTask);
        timeSlotRepository.save(slot);

        return true;
    }
}
