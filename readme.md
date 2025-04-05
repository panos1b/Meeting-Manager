# Meeting Manager

## Overview

This project is a meeting management application that uses a key-value store to 
handle active meetings and their chat rooms. The application is designed for physical 
meetings that take place at specific geographical coordinates during a scheduled time 
interval. Meeting administrators create meetings, and once a meeting starts, a chat 
room is available for participants to communicate.

## Features

- **User and Meeting Creation**: Users can register and meetings can be created with details such as title, description, start and end times, and location coordinates.
- **Active Meeting Management**: Meetings become active at their scheduled time. Active meetings are managed in Redis, where participant details and chat room messages are stored.
- **Nearby Meeting Search**: A function allows users to find active meetings near their current location (within 100 meters).
- **Joining and Leaving Meetings**: Users can join or leave meetings, with appropriate logging for each action.
- **Chat Room Functionality**: Each active meeting has a chat room. Users can send and view messages, with messages displayed in chronological order.
- **Meeting Termination**: Meetings can be ended, which updates the status for all remaining participants and logs time-out actions.

## Technologies Used

- **Frontend**: Bootstrap 5 provides a clean and responsive user interface.
- **Backend**: Java Spring Boot with Thymeleaf is used to create dynamic web pages.
- **Build Tool**: Maven is used for packaging and running the project.
- **Hot Store**: Redis is used for managing active meetings and chat messages.
- **Cold Store**: SQLite is used for long term data storage.

## Setup Instructions
#### Run Sequence

1. **Install Redis Community Edition**  
   Ensure that the Redis server is installed and running.

2. **Build and Run the Application**  
   Use Maven to run the Spring Boot application with the following command:
   ```
   mvn spring-boot:run
   ```

3. **Access the Application**  
   Open a web browser and navigate to [localhost](http://localhost:8080).
    - The index page will display forms for creating meetings and users, joining or leaving meetings, finding nearby meetings, and ending meetings.
    - The meeting page provides a list of active meeting.
    - The chat page provides an interface for sending and viewing messages during an active meeting.
   
#### Shutdown Sequence
Even if you don't terminate Redis you **must** allow the shutdown sequence of Spring Boot to complete after
ending the build. If you don't you might encounter errors with the logs both in the Database and in Redis.
If this happens just run `FLUSHALL` in a redis terminal connected to your redis server. 
WARNING THIS WILL DELETE ALL REDIS KEY - VALUES! Some logs will be permanently lost.

## Project Context

The project is a part of an assignment on Redis and key-value stores as part 
of the [Big Data Management Systems](https://www.dept.aueb.gr/en/dmst/content/big-data-management-systems) course.

## Licencing

- Unless otherwise stated all of this repository's code is covered by the EUROPEAN UNION PUBLIC LICENCE v. 1.2
- My permanent address is in Greece, so as per EUPL v1.2 the governing law is the Greek law

## Limitations
Keep in mind the goal of this project is to use Redis. You should understand that everything else is treated as an
afterthought. This is why the frontend is plain, the MVC framework loosely followed, 
simple SQLite is used for permanent storage etc.

## Thanks
Special thanks to Redi (without the s) for the help!
