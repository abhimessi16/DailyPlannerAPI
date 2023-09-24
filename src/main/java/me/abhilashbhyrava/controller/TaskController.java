package me.abhilashbhyrava.controller;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Task;
import me.abhilashbhyrava.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/planner/{username}/time-slot/{timeslot}/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable int id){
        Task task = taskService.getTask(id);

        return ResponseEntity
                .status(task != null ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST)
                .body(task);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Task>> getAllTasks(@PathVariable String username
            , @PathVariable(name = "timeslot") int timeSlot){
        return ResponseEntity.ok(taskService.getAllTasks(username, timeSlot));
    }

    @PostMapping("/add")
    public ResponseEntity<Task> addTask(@PathVariable String username
            , @PathVariable(name = "timeslot") int timeSlot
            , @RequestBody Task task){
        return ResponseEntity.ok(taskService.addTask(username, timeSlot, task));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable String username
            , @PathVariable(name = "timeslot") int timeSlot
            , @PathVariable int id){

        boolean taskDeleted = taskService.deleteTask(username, timeSlot, id);

        return ResponseEntity
                .status(taskDeleted ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST)
                .body((taskDeleted ? "Deleted task with id: "
                        : "No task with id: ") + id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateTask(@PathVariable String username
            , @PathVariable(name = "timeslot") int timeSlot
            , @PathVariable int id
            , @RequestBody Task task){

        boolean taskUpdated = taskService.updateTask(username, timeSlot, id, task);

        return ResponseEntity
                .status(taskUpdated ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST)
                .body(taskUpdated ? "Task updated!" : "Task doesn't exist!");

    }

}
