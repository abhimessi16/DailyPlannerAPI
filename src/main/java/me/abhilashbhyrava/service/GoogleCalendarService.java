package me.abhilashbhyrava.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import lombok.RequiredArgsConstructor;
import me.abhilashbhyrava.model.Task;
import me.abhilashbhyrava.model.TaskStatus;
import me.abhilashbhyrava.model.TimeSlot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//@Service
//@RequiredArgsConstructor
public class GoogleCalendarService {

    private Calendar calendar;

    public GoogleCalendarService(String accessToken){

        HttpRequestInitializer requestInitializer = request -> request.getHeaders()
                .setAuthorization("Bearer " + accessToken);

        try{
            this.calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    null
            )
                    .setHttpRequestInitializer(requestInitializer)
                    .setApplicationName("Daily Planner")
                    .build();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    // we will create a new calendar to keep the events of this app separate
    public String checkDailyPlannerCalendarPresent() throws IOException{
        CalendarList calendarList = this.calendar.calendarList().list().execute();
        Optional<CalendarListEntry> dailyPlannerCalendar =
                calendarList.getItems().stream().filter(cal -> cal.getSummary().equals("Daily Planner")).findFirst();

        if(dailyPlannerCalendar.isEmpty()){
            com.google.api.services.calendar.model.Calendar cal = new com.google.api.services.calendar.model.Calendar();
            cal
                    .setSummary("Daily Planner")
                    .setDescription("For Daily Planner app");
            com.google.api.services.calendar.model.Calendar c = this.calendar.calendars().insert(cal).execute();
            return c.getId();
        }
        return dailyPlannerCalendar.get().getId();
    }

    public void addTask(int timeSlot, Task task) {

        try {

            String calendarId = checkDailyPlannerCalendarPresent();

            Event event = new Event()
                    .setSummary(task.getTaskName())
                    .setDescription(task.getTaskDescription());

            event.setStart(getEventDateTime(timeSlot));
            event.setEnd(getEventDateTime(timeSlot + 1));

            this.calendar.events().insert(calendarId, event).execute();

        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public List<Task> syncTasksToAdd(TimeSlot timeSlot){

        Set<String> tasks = timeSlot.getTasks().stream().map(Task::getTaskName).collect(Collectors.toSet());
        List<Task> addTasks = new ArrayList<>();

        String zone = "Asia/Kolkata";
        ZoneId zoneId = ZoneId.of(zone);
        ZoneOffset zoneOffset = zoneId.getRules().getOffset(LocalDateTime.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss".concat(zoneOffset.toString()));

        try {

            LocalDateTime minLocalDateTime = LocalDateTime
                    .of(LocalDate.now(), LocalTime.of(timeSlot.getStartTime(), 0));
            DateTime minDateTime = new DateTime(minLocalDateTime.format(formatter));

            LocalDateTime maxLocalDateTime = LocalDateTime
                    .of(LocalDate.now(), LocalTime.of(timeSlot.getStartTime() + 1, 0));
            DateTime maxDateTime = new DateTime(maxLocalDateTime.format(formatter));

            // getting all events available in the calendar
            String calendarId = checkDailyPlannerCalendarPresent();
            Events eventList = this.calendar.events().list(calendarId)
                    .setTimeMin(minDateTime)
                    .setTimeMax(maxDateTime)
                    .execute();

            for(Event event : eventList.getItems()){

                int hour = getEventDateTimeHour(event.getStart());

                if(!tasks.contains(event.getSummary()) && hour == timeSlot.getStartTime()){
                    Task task = new Task();
                    task.setTaskName(event.getSummary());
                    task.setTaskDescription(event.getDescription());
                    task.setTaskStatus(TaskStatus.PENDING);

                    addTasks.add(task);
                }
            }

        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        return addTasks;
    }

    public Set<Integer> syncTasksToRemove(TimeSlot timeSlot){

        Map<String, Integer> tasks =
                timeSlot.getTasks().stream().collect(Collectors.toMap(Task::getTaskName, Task::getTaskId));
        Set<Integer> removeTasks = new HashSet<>();

        String zone = "Asia/Kolkata";
        ZoneId zoneId = ZoneId.of(zone);
        ZoneOffset zoneOffset = zoneId.getRules().getOffset(LocalDateTime.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss".concat(zoneOffset.toString()));

        try {

            String calendarId = checkDailyPlannerCalendarPresent();

            LocalDateTime minLocalDateTime = LocalDateTime
                    .of(LocalDate.now(), LocalTime.of(timeSlot.getStartTime(), 0));
            DateTime minDateTime = new DateTime(minLocalDateTime.format(formatter));

            LocalDateTime maxLocalDateTime = LocalDateTime
                    .of(LocalDate.now(), LocalTime.of(timeSlot.getStartTime() + 1, 0));
            DateTime maxDateTime = new DateTime(maxLocalDateTime.format(formatter));

            Events eventList = this.calendar.events().list(calendarId)
                    .setTimeMax(maxDateTime)
                    .setTimeMin(minDateTime)
                    .execute();
            Set<String> eventSet = new HashSet<>(eventList.getItems().stream().map(Event::getSummary).toList());

            for(String task : tasks.keySet()){
                if(!eventSet.contains(task))
                    removeTasks.add(tasks.get(task));
            }


        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        return removeTasks;
    }

    public static int getEventDateTimeHour(EventDateTime eventDateTime){
        String d = eventDateTime.getDateTime().toString().split("T")[1];

        System.out.println(d);
        LocalTime one = LocalTime.parse(d.substring(0, 5));
        int offsetHour = 5;
        int offsetMin = 30;

        one = one.plusHours(offsetHour);
        one = one.plusMinutes(offsetMin);

        return one.getHour();
    }

    public EventDateTime getEventDateTime(int hour){

        String zone = "Asia/Kolkata";
        ZoneId zoneId = ZoneId.of(zone);
        ZoneOffset zoneOffset = zoneId.getRules().getOffset(LocalDateTime.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss".concat(zoneOffset.toString()));

        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, 0));
        DateTime endDateTime = new DateTime(endTime.format(formatter));
        return new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Kolkata");
    }

    // test
    public void getAllEvents(){

        try{

            Events eventList = this.calendar.events().list("primary").execute();
            for(Event event : eventList.getItems()){
                System.out.println(event.getSummary());
            }
            System.out.println(eventList);

        }catch(IOException e){
            System.out.println(e.getMessage());
        }

    }

}
