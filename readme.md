<h1>Daily Planner</h1>

A day-to-day tasks planner app.<br><br>

You tell DP (Daily Planner) the duration of the day for which you want to plan your tasks and DP will create time slots for each hour from the start of your day to the end of your day.
<br>The user is hereafter referred to as  the 'Planner'.
<br>All times are in 24-hour format.
<br>

Each timeslot will span one hour of the total duration and each timeslot will contain the tasks that are to be performed by the Planner in that timeslot (hour).

Say,<br>
StartTime = 10<br>
EndTime = 12<br>
Here, 2 TimeSlots will be created.
<br>=> 10 to 11
<br>=> 11 to 12
<br>

<h3>This is the API which acts as the server for the app.</h3>

The api exposes these endpoints.

<b> For Planners </b>

1) GET /api/v1/planner/:username

2) GET /api/v1/planner/all

3) POST /api/v1/planner/add

4) DELETE /api/v1/planner/delete/:username

5) PUT /api/v1/planner/update/:username

<b> For Timeslots </b>

1) GET /api/v1/planner/:username/time-slot/:timeslot

2) GET /api/v1/planner/:username/time-slot/all

<b> For Tasks </b>

1) GET /api/v1/planner/:username/time-slot/:timeslot/task/:id

2) GET /api/v1/planner/:username/time-slot/:timeslot/task/all

3) POST /api/v1/planner/:username/time-slot/:timeslot/task/add

4) DELETE /api/v1/planner/:username/time-slot/:timeslot/task/:id

5) PUT /api/v1/planner/:username/time-slot/:timeslot/task/:id
