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
    id uuid not null,
    is_private bool not null default false,
    name varchar(200) not null,
    description varchar,
    owner_id uuid not null,
    primary key (id),
    foreign key (owner_id) references users(id)
);

create table meeting_schedule
(
    id uuid not null,
    meeting_id uuid not null,
    schedule_id uuid not null,
    primary key (id),
    foreign key (meeting_id) references meeting(id),
    foreign key (schedule_id) references schedule(id),
    unique (meeting_id, schedule_id)
);

create table meeting_member
(
    id uuid not null,
    user_id uuid not null,
    meeting_id uuid not null,
    is_confirmed bool not null default false,
    is_canceled bool not null default false,
    primary key (id),
    foreign key (user_id) references users(id),
    foreign key (meeting_id) references meeting(id),
    unique (user_id, meeting_id)
);

-- mocks
-- "password": "adminPassword"
insert into users(id, name, surname, login, email, password, zone_offset, is_enabled)
values ('196a26f4-95b1-4fd8-1000-000000000000', 'admin', 'adminich', 'admin', 'admin@admin.admin',
        '$2a$10$DM4ktkMr7Puqan.xBgqi0eNG0MS4CpqnpcI/dDweT8/5MvuauEnAq', '+03:00', true);
insert into user_authorities(id, user_id, authority)
values ('196a26f4-95b1-4fd8-2000-000000000000', '196a26f4-95b1-4fd8-1000-000000000000', 'admin');