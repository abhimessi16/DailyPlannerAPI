package me.abhilashbhyrava.controller;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Planner;
import me.abhilashbhyrava.service.PlannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    @GetMapping("/{username}")
    public ResponseEntity<Planner> getPlanner(@PathVariable String username){

        Planner planner = plannerService.getPlanner(username);

        return ResponseEntity
                .status(
                        planner != null ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
                ).header("status",
                        planner != null ? " is present!" : " doesn't exist"
                )
                .body(planner);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Planner>> allPlanners(){
        return ResponseEntity.ok(plannerService.getPlanners());
    }

    @PostMapping("/add")
    public ResponseEntity<String> addPlanner(@RequestBody Planner plannerToAdd){

        boolean plannerAdded = plannerService.addPlanner(plannerToAdd);

        return ResponseEntity
                .status(
                        plannerAdded ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
                )
                .body(
                        plannerToAdd.getUsername()
                                .concat(plannerAdded ? " is added!" : " is already present!"
                )
        );
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> removePlanner(@PathVariable String username){

        boolean plannerRemoved = plannerService.removePlanner(username);

        return ResponseEntity
                .status(
                        plannerRemoved ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
                )
                .body(
                        username.concat(
                                plannerRemoved ? " is removed!"
                                : " doesn't exist!")
                );
    }

    @PutMapping("/update/{username}")
    public ResponseEntity<String> updatePlanner(@PathVariable String username, @RequestBody Planner plannerToUpdate){

        int plannerUpdated = plannerService.updatePlanner(username, plannerToUpdate);

        return ResponseEntity
                .status(
                        plannerUpdated == 2 ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST
                ).body(
                        username
                                .concat(plannerUpdated == 2 ? " is updated!"
                                        : (plannerUpdated == 0 ? " doesn't exist"
                                        : " : username mismatch in request url and body")
                                )
                );
    }

    @GetMapping("/{username}/status")
    public ResponseEntity<String> plannerStatus(@PathVariable String username){

        // [0] -> total tasks, [1] -> finished tasks
        int[] taskCounts = plannerService.getPlannerStatus(username);

        if(taskCounts.length == 0){
            return ResponseEntity.badRequest()
                    .body(username
                            .concat(": No such planner present")
                    );
        }

        return ResponseEntity.ok()
                .header("All-Task-Count", String.valueOf(taskCounts[0]))
                .header("All-Finish-Task-Count", String.valueOf(taskCounts[1]))
                .body("Details in header!");
    }

}
