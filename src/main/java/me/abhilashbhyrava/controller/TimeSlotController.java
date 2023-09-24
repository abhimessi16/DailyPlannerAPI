package me.abhilashbhyrava.controller;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.TimeSlot;
import me.abhilashbhyrava.service.TimeSlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/planner/{username}/time-slot")
@RequiredArgsConstructor
public class TimeSlotController {

    // time slots won't be created or deleted outside of planner
    // it makes sense to only display timeslots
    // and to add or remove tasks
    // update task status

    private final TimeSlotService timeSlotService;

    @GetMapping("/{timeslot}")
    public ResponseEntity<TimeSlot> getTimeSlot(@PathVariable String username, @PathVariable(name = "timeslot") int timeSlot){
        TimeSlot slot = timeSlotService.getTimeSlot(username, timeSlot);

        return ResponseEntity
                .status(slot != null ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST)
                .body(slot);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TimeSlot>> getAllTimeSlots(@PathVariable String username){
        return ResponseEntity.ok(timeSlotService.getAllTimeSlots(username));
    }

}
