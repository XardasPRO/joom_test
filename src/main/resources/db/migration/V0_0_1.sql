CREATE TABLE users
(
    id         uuid         not null unique,
    name       varchar(20)  not null,
    surname    varchar(20)  not null,
    login      varchar(20)  not null unique,
    email      varchar(30)  not null unique,
    password   varchar(100) not null,
    timezone   smallint     not null default 0,
    is_enabled bool         not null default true,
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
)