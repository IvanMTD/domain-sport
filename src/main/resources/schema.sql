create table if not exists ekp(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    ekp text,
    num text,
    title text,
    description text,
    status text,
    beginning date,
    ending date,
    category text,
    location text,
    organization text,
    sport_id bigint,
    discipline_ids bigint[],
    logo bigint,
    image bigint,
    s float,
    d float
);

create table if not exists sport_object(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    title text,
    location text,
    address text,
    register_date date,
    url text,
    s float,
    d float,
    logo_id bigint,
    image_ids bigint[]
);

create table if not exists sport(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    title text,
    description text,
    season text,
    sport_status text,
    discipline_ids bigint[]
);

create table if not exists discipline(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    title text,
    description text,
    sport_id bigint
);

create table if not exists app_user(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    username text not null unique,
    password text not null,
    firstname text,
    lastname text,
    email text unique,
    birthday date,
    placed_at date,
    avatar_id text,
    oauth_id text,

    role text,
    role_access_ids bigint[],
    politic_accept bool
);

create table if not exists role_access(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    user_id bigint,

    access text,
    group_id bigint,
    permission_list text[],

    object_access_ids bigint[]
);

create table if not exists object_access(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    role_access_id bigint,
    class_name text,
    object_id bigint
);

create table if not exists minio_file (
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    mid bigint,
    uid text,
    name text,
    type text,
    e_tag text,
    bucket text,
    path text,
    minio_url text,
    file_size float
);

create table if not exists auth_token (
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    link text,
    email text,
    status bool,
    role text,
    access text,
    sport_id bigint
);

create table if not exists news (
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    author_id bigint,

    title text,
    annotation text,
    content text,
    placed_at date
);

create table if not exists school (
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    name text,
    ogrn bigint,
    index int,
    address text,
    url text,
    s float,
    d float,

    subject text,
    description text,

    logo_id bigint,
    photo_id bigint
);