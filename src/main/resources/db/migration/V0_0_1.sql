CREATE TABLE users
(
    id          uuid         not null unique,
    name        varchar(20)  not null,
    surname     varchar(20)  not null,
    login       varchar(20)  not null unique,
    email       varchar(30)  not null unique,
    password    varchar(100) not null,
    zone_offset varchar(6)   not null default '00:00',
    is_enabled  bool         not null default true,
    primary key (id)
);

create table user_authorities
(
    id        uuid        not null unique,
    user_id   uuid        not null,
    authority varchar(20) not null,
    primary key (id),
    unique (user_id, authority),
    foreign key (user_id) references users (id)
);

create table schedule
(
    id              uuid        not null,
    type            varchar(20) not null,
    start_date_time timestamptz not null,
    duration        bigint      not null,
    is_repeatable   bool        not null default false,
    primary key (id)
);

create table user_working_schedule
(
    id          uuid not null,
    user_id     uuid not null,
    schedule_id uuid not null,
    primary key (id),
    foreign key (user_id) references users (id),
    foreign key (schedule_id) references schedule (id),
    unique (user_id, schedule_id)
);

create table meeting
(
    id          uuid         not null,
    is_private  bool         not null default false,
    name        varchar(200) not null,
    description varchar,
    owner_id    uuid         not null,
    primary key (id),
    foreign key (owner_id) references users (id)
);

create table meeting_schedule
(
    id          uuid not null,
    meeting_id  uuid not null,
    schedule_id uuid not null,
    primary key (id),
    foreign key (meeting_id) references meeting (id),
    foreign key (schedule_id) references schedule (id),
    unique (meeting_id, schedule_id)
);

create table meeting_member
(
    id           uuid not null,
    user_id      uuid not null,
    meeting_id   uuid not null,
    is_confirmed bool not null default false,
    is_canceled  bool not null default false,
    primary key (id),
    foreign key (user_id) references users (id),
    foreign key (meeting_id) references meeting (id),
    unique (user_id, meeting_id)
);

-- mocks
-- "password": "adminPassword"
insert into users(id, name, surname, login, email, password, zone_offset, is_enabled)
values ('196a26f4-95b1-4fd8-1000-000000000000', 'admin', 'adminich', 'admin', 'admin@admin.admin',
        '$2a$10$DM4ktkMr7Puqan.xBgqi0eNG0MS4CpqnpcI/dDweT8/5MvuauEnAq', '+03:00', true);
insert into user_authorities(id, user_id, authority)
values ('196a26f4-95b1-4fd8-2000-000000000000', '196a26f4-95b1-4fd8-1000-000000000000', 'admin');


-- test users
insert into users(id, name, surname, login, email, password, zone_offset, is_enabled)
values ('196a26f4-95b1-4fd8-1000-000000000001', 'user_1', 'adminich', 'user1', 'user1@test.mail',
        '$2a$10$DM4ktkMr7Puqan.xBgqi0eNG0MS4CpqnpcI/dDweT8/5MvuauEnAq', '+00:00', true);
insert into users(id, name, surname, login, email, password, zone_offset, is_enabled)
values ('196a26f4-95b1-4fd8-1000-000000000002', 'user_2', 'adminich', 'user2', 'user2@test.mail',
        '$2a$10$DM4ktkMr7Puqan.xBgqi0eNG0MS4CpqnpcI/dDweT8/5MvuauEnAq', '+03:00', true);
insert into users(id, name, surname, login, email, password, zone_offset, is_enabled)
values ('196a26f4-95b1-4fd8-1000-000000000003', 'user_3', 'adminich', 'user3', 'user3@test.mail',
        '$2a$10$DM4ktkMr7Puqan.xBgqi0eNG0MS4CpqnpcI/dDweT8/5MvuauEnAq', '-03:00', true);


insert into user_authorities(id, user_id, authority)
values ('196a26f4-95b1-4fd8-2000-000000000001', '196a26f4-95b1-4fd8-1000-000000000001', 'user');
insert into user_authorities(id, user_id, authority)
values ('196a26f4-95b1-4fd8-2000-000000000002', '196a26f4-95b1-4fd8-1000-000000000002', 'user');
insert into user_authorities(id, user_id, authority)
values ('196a26f4-95b1-4fd8-2000-000000000003', '196a26f4-95b1-4fd8-1000-000000000003', 'user');

-- users working schedule
--first user
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000000', 'WORKDAYS', '1986-04-08 10:00:00.000000 +00:00', 14400, true);
insert into user_working_schedule (id, user_id, schedule_id)
values ('196a26f4-95b1-4fd8-0002-000000000000', '196a26f4-95b1-4fd8-1000-000000000001',
        '196a26f4-95b1-4fd8-1100-000000000000');
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000001', 'WORKDAYS', '1986-04-08 15:00:00.000000 +00:00', 14400, true);
insert into user_working_schedule (id, user_id, schedule_id)
values ('196a26f4-95b1-4fd8-0002-000000000001', '196a26f4-95b1-4fd8-1000-000000000001',
        '196a26f4-95b1-4fd8-1100-000000000001');
--second user
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000002', 'WORKDAYS', '1986-04-08 07:00:00.000000 +00:00', 14400, true);
insert into user_working_schedule (id, user_id, schedule_id)
values ('196a26f4-95b1-4fd8-0002-000000000002', '196a26f4-95b1-4fd8-1000-000000000002',
        '196a26f4-95b1-4fd8-1100-000000000002');
--third user
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000003', 'WORKDAYS', '1986-04-08 13:00:00.000000 +00:00', 14400, true);
insert into user_working_schedule (id, user_id, schedule_id)
values ('196a26f4-95b1-4fd8-0002-000000000003', '196a26f4-95b1-4fd8-1000-000000000003',
        '196a26f4-95b1-4fd8-1100-000000000003');
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000004', 'SUNDAY', '1986-04-08 10:00:00.000000 +00:00', 7200, true);
insert into user_working_schedule (id, user_id, schedule_id)
values ('196a26f4-95b1-4fd8-0002-000000000004', '196a26f4-95b1-4fd8-1000-000000000003',
        '196a26f4-95b1-4fd8-1100-000000000004');

-- users
--
insert into meeting (id, is_private, name, description, owner_id)
values ('196a26f4-95b1-4fd8-3300-000000000000', false, 'daily meeting', 'daily meeting description',
        '196a26f4-95b1-4fd8-1000-000000000001');
insert into meeting_member(id, user_id, meeting_id, is_confirmed, is_canceled)
VALUES ('196a26f4-95b1-4fd8-3301-000000000000', '196a26f4-95b1-4fd8-1000-000000000001',
        '196a26f4-95b1-4fd8-3300-000000000000', true, false);
insert into meeting_member(id, user_id, meeting_id, is_confirmed, is_canceled)
VALUES ('196a26f4-95b1-4fd8-3301-000000000001', '196a26f4-95b1-4fd8-1000-000000000002',
        '196a26f4-95b1-4fd8-3300-000000000000', false, true);
insert into meeting_member(id, user_id, meeting_id, is_confirmed, is_canceled)
VALUES ('196a26f4-95b1-4fd8-3301-000000000002', '196a26f4-95b1-4fd8-1000-000000000003',
        '196a26f4-95b1-4fd8-3300-000000000000', false, false);
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000005', 'WORKDAYS', '1986-04-08 10:00:00.000000 +00:00', 600, true);
insert into meeting_schedule (id, meeting_id, schedule_id)
values ('196a26f4-95b1-4fd8-3330-000000000000', '196a26f4-95b1-4fd8-3300-000000000000',
        '196a26f4-95b1-4fd8-1100-000000000005');
--
insert into meeting (id, is_private, name, description, owner_id)
values ('196a26f4-95b1-4fd8-3300-000000000001', false, 'user_2_meeting', '',
        '196a26f4-95b1-4fd8-1000-000000000002');
insert into meeting_member(id, user_id, meeting_id, is_confirmed, is_canceled)
VALUES ('196a26f4-95b1-4fd8-3301-000000000003', '196a26f4-95b1-4fd8-1000-000000000002',
        '196a26f4-95b1-4fd8-3300-000000000001', true, false);
insert into schedule(id, type, start_date_time, duration, is_repeatable)
VALUES ('196a26f4-95b1-4fd8-1100-000000000006', 'DATE', '2022-01-10 08:00:00.000000 +00:00', 1800, true);
insert into meeting_schedule (id, meeting_id, schedule_id)
values ('196a26f4-95b1-4fd8-3330-000000000001', '196a26f4-95b1-4fd8-3300-000000000001',
        '196a26f4-95b1-4fd8-1100-000000000006');