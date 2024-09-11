drop table if exists "t_ingredients";
create table "t_ingredients"
(
    "id"         uuid primary key default gen_random_uuid(),
    "name"       text             not null,
    "created_at" timestamp        default now(),
    "updated_at" timestamp        default now()
);

insert into "t_ingredients" ("name") values ('dark chocolate');
insert into "t_ingredients" ("name") values ('milk chocolate');
insert into "t_ingredients" ("name") values ('whipped cream');
insert into "t_ingredients" ("name") values ('hazelnuts');