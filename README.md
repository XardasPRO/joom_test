# Calendar
### Setup guide
* build project .jar artifact by gragle
* setup database (required postgresql)
* configure app config file
* run java -jar calendar-0.0.1-SNAPSHOT.jar

###config description
```
server:
  port: 8080  //application web port
calendar:
  search-limit-days: 30  //limit at days for search closest free time for meeting
  check-work-time-limit: true  //if true - check users's work scheduler is more then meeting duration
  max-working-period-duration: 86400  //Max duration of user's working period at sec
  max-meeting-duration: 86400 //Max meeting duration at sec
  meeting-schedule-items-limit: 20 //limit of schedule items at user work schedule
spring:
  security:
    token-lifetime: 86400  //token life time at sec
    cookie-name: calendarAuthCookie  //cookie with token name
    cookie-token-signing-key: secretkeyforsubscribeauthtoken  //secret key for subscribe token
  datasource:
    url: jdbc:postgresql://192.168.1.100:7689/postgres  //db connection string
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: calendar
        jdbc:
          time_zone: UTC
  jackson:
    serialization:
      fail-on-empty-beans: false
    mapper:
      accept-case-insensitive-enums: true
  flyway:
    schemas: calendar
```
##Schedule item and schedule combinations
Schedule for meeting and working schedule describes by combination of `Schedule` items.
```
{
"type": "WORKDAYS", //type of schedule, supports:
                        DATE, - concrete date
                        DAILY, - every day
                        WORKDAYS, - work days
                        WEEKENDS, - weekends
                        MONDAY, - equals to day of week
                        TUESDAY,
                        WEDNESDAY,
                        THURSDAY,
                        FRIDAY,
                        SATURDAY,
                        SUNDAY
"startDateTime": "1986-04-08T15:00", //the date and time of first schedule event
"duration": 14400, //duration of the schedule event
"isRepeatable": true //flag of repeant
}
```
By combinations of this event's we can configure user's working schedule and meetings schedule. For example:

- user schedule with lunch
```
"schedule": [
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T10:00",
            "duration": 14400,
            "isRepeatable": true
        },
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T15:00",
            "duration": 14400,
            "isRepeatable": true
        }
    ]
```
- daily morning meeting
```
"schedule": [
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T10:00",
            "duration": 600,
            "isRepeatable": true
        }
        ]
```
- meeting with concrete date
```
"schedule": [
        {
            "type": "DATE",
            "startDateTime": "2022-04-08T10:00",
            "duration": 1200,
            "isRepeatable": true
        }
        ]
```
- meeting only on mondays and fridays
```
"schedule": [
        {
            "type": "MONDAY",
            "startDateTime": "2022-04-08T17:00",
            "duration": 3600,
            "isRepeatable": true
        },
        {
            "type": "FRIDAY",
            "startDateTime": "2022-04-08T16:00",
            "duration": 3600,
            "isRepeatable": true
        }
        ]
```
###By adding new schedule type's and evaluators for them, we can expand the scheduling functionality 
##Rest description
- `/login`
- POST
- Required user credentials and returns authorization token cookie at response
- ```
  {
    "login": "admin",  //user's login
    "password": "adminPassword"  //users'password
    }
---------------
- `/user/create`
- POST
- Create new user. Required admin role.
- ```
  {
    "name": "test",  //new user name
    "surname": "testovich",  //new user surname
    "login": "user",  //new user login
    "password": "adminPassword",  //new user password 
    "email": "user@mail.ru", //new user email
    "zoneOffset": "+01:00",  //new user time zone offset from UTC
    "authorities": [  //user roles
        "user", "admin"
    ]
}
---------------
- `/user/update_schedule`
- POST
- Update user working schedule
- ```
  {
    "userId": "196a26f4-95b1-4fd8-1000-000000000000",  //user id
    "zoneOffset": "+00:00",  //user zone offset
    "schedule": [  //user's working schedule
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T10:00",
            "duration": 14400,
            "isRepeatable": true
        },
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T15:00",
            "duration": 14400,
            "isRepeatable": true
        }
    ]
}
---------------
- `/meeting/create`
- POST
- Create meeting
- ```
  {
    "isPrivate": true,  //flag of meeting visability
    "name": "TestMeeting",  //meeting name
    "description": "test meeting description", //meeting description
    "schedule": [  //meeting schedule
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T10:00",
            "duration": 14400,
            "isRepeatable": true
        },
        {
            "type": "WORKDAYS",
            "startDateTime": "1986-04-08T15:00",
            "duration": 14400,
            "isRepeatable": true
        }
    ],
    "members": [ //invited members id's
        "b2234cf6-710c-4a5c-a814-1b07b8c0ad99"
    ]
}
---------------
- `/meeting/get?meetingId=d7e5ff9a-039d-4306-b435-b6516c5a1090`
- GET
- Returns meeting structure, if this is private meeting, and you are not participant of it - you receive short description.
- ```
  meetingId - id of required meeting
}
---------------
- `/meeting/cancel?meetingId=d7e5ff9a-039d-4306-b435-b6516c5a1090`
- GET
- Cancel meeting
- ```
  meetingId - id of required meeting
}
---------------
- `/meeting/confirm?meetingId=d7e5ff9a-039d-4306-b435-b6516c5a1090`
- GET
- Confirm meeting
- ```
  meetingId - id of required meeting
}
---------------
- `/meeting/user-schedule?userId=196a26f4-95b1-4fd8-1000-000000000002&from=2022-01-08T08:00:00.000000&to=2022-01-11T15:00:00.000000`
- GET
- Get user's meetings at time range
- ```
  userId - id of required user
  from - date from start
  to - end fate
}
---------------
- `meeting/find-time`
- POST
- Find the closest free time for new meeting 
- ```
  {
    "duration": 6000, //duration of the meeting
    "members": [ //set of user's id
        "196a26f4-95b1-4fd8-1000-000000000001",
        "196a26f4-95b1-4fd8-1000-000000000002",
        "196a26f4-95b1-4fd8-1000-000000000003"
    ]
}