package me.abhilashbhyrava.controller;

import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Planner;
import me.abhilashbhyrava.service.GoogleCalendarService;
import me.abhilashbhyrava.service.PlannerService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    // test endpoint, checking if oauth2 login is successful
    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", principal.getAttribute("name"));
    }

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
    public ResponseEntity<List<String>> allPlanners(){
        return ResponseEntity.ok(plannerService.getPlannerNames());
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
    public ResponseEntity<String> plannerStatus(@PathVariable String username) throws JSONException {

        // [0] -> total tasks, [1] -> finished tasks
        int[] taskCounts = plannerService.getPlannerStatus(username);

        if(taskCounts.length == 0){
            return ResponseEntity.badRequest()
                    .body(username
                            .concat(": No such planner present")
                    );
        }

        JSONObject obj = new JSONObject();
        obj.put("All-Task-Count", taskCounts[0]);
        obj.put("All-Task-Finish-Count", taskCounts[1]);

        return ResponseEntity.ok()
                .body(obj.toString());
    }

    @GetMapping("/{username}/sync")
    public ResponseEntity<String> syncGoogleCalendar(@PathVariable String username
            , @RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient client){

        // for now let's assume this works and that Google OAuth2 is the only way for authentication
        String accessToken = client.getAccessToken().getTokenValue();
        plannerService.syncWithGoogleCalendar(accessToken, username);

        return ResponseEntity.ok("Working!");
    }

}
