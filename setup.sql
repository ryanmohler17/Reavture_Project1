create table if not exists "user"
(
    id serial
        constraint user_pk
        primary key,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    username varchar(50) not null,
    email_address varchar(60) not null,
    password varchar(100) not null,
    user_type integer not null
);

alter table "user" owner to postgres;

create unique index if not exists user_username_uindex
    on "user" (username);

create table if not exists reimbursement_request
(
    id serial
        constraint reimbursement_request_pk
        primary key,
    employee integer
        constraint reimbursement_request_user_id_fk
        references "user",
    submitted timestamp not null,
    status integer not null,
    last_update timestamp not null,
    resolved_by integer
        constraint reimbursement_request_user_id_fk_2
        references "user"
);

alter table reimbursement_request owner to postgres;

create table if not exists reimbursement_part
(
    id serial
        constraint reimbursement_part_pk
        primary key,
    amount numeric(10,2) not null,
    description text,
    type integer not null,
    rate numeric(4,2)
);

alter table reimbursement_part owner to postgres;

create table if not exists part_request_link
(
    request_id integer not null
        constraint part_request_link_reimbursement_request_id_fk
        references reimbursement_request,
    part_id integer not null
        constraint part_request_link_reimbursement_part_id_fk
        references reimbursement_part,
    constraint part_request_link_pk
        unique (request_id, part_id)
);

alter table part_request_link owner to postgres;

create table if not exists image_store
(
    image_id uuid not null
        constraint image_store_pk
        primary key,
    image text not null
);

alter table image_store owner to postgres;

create table if not exists part_img
(
    part_id integer not null
        constraint part_img_reimbursement_part_id_fk
        references reimbursement_part,
    image_id uuid not null
        constraint part_img_image_store_image_id_fk
        references image_store,
    constraint part_img_pk
        unique (part_id, image_id)
);

alter table part_img owner to postgres;

create table if not exists user_icon
(
    user_id integer not null
        constraint user_icon_pk
        primary key
        constraint user_icon_user_id_fk
        references "user",
    image_id uuid
        constraint user_icon_image_store_image_id_fk
        references image_store
);

alter table user_icon owner to postgres;

INSERT INTO "user" (id, first_name, last_name, username, email_address, password, user_type)
VALUES (DEFAULT, 'Admin', 'User', 'admin', 'admin@example.com',
        '$2a$12$tKGdG9Kp1/5P59JFLTEK5eLo1fL3lve4d60Dmee5q./wVILq5Xzwq', 1);
