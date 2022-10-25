create table sample
(
    id         bigint auto_increment primary key,
    created_at datetime(3) null,
    updated_at datetime(3) null,
    version    bigint null,
    title      varchar(50) null
);