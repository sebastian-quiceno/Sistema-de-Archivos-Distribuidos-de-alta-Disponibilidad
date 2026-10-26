-- =====================================
-- === Creacion de Usuario Midleware ===
-- =====================================

-- Creacion de la base de datos
CREATE DATABASE metadata_database;

-- Creacion del esquema
CREATE SCHEMA metadata_database.middleware;

-- Creacion del usuario (Login)
CREATE USER middleware_admin WITH PASSWORD 'middlewarepassword';

-- Permiso para el usuario "middleware_admin" para entrar a la base de datos
GRANT CONNECT ON DATABASE "metadata_database" TO middleware_admin;

-- Permiso para entrar a la oficina (Usar el esquema)
GRANT USAGE ON SCHEMA metadata_database.middleware TO middleware_admin;

-- Privilegios minimos operativos para tablas ACTUALES (CRUD: Select, Insert, Update, Delete)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA metadata_database.middleware TO middleware_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA metadata_database.middleware TO middleware_admin;

-- Privilegios por defecto para tablas FUTURAS
ALTER DEFAULT PRIVILEGES IN SCHEMA metadata_database.middleware
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO middleware_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA metadata_database.middleware
    GRANT USAGE, SELECT ON SEQUENCES TO middleware_admin;


-- ===============================
-- === Creacion de las tablas ====
-- ===============================

-- Tablas independientes

CREATE TABLE IF NOT EXISTS metadata_database.middleware.user(
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username VARCHAR(30) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    date_created DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS metadata_database.middleware.file_type(
    file_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(20) NOT NULL,
    description VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS metadata_database.middleware.status(
    status_id UUID NOT NULL DEFAULT gen_random_uuid(),

    name VARCHAR(20) NOT NULL,
    description VARCHAR(50) NOT NULL
);

-- Tablas cruzadas

CREATE TABLE IF NOT EXISTS metadata_database.middleware.folder(
    folder_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    dad_folder_id UUID,

    name VARCHAR(50) NOT NULL,
    date_created DATE NOT NULL,

    FOREIGN KEY (user_id) REFERENCES metadata_database.middleware.user(user_id),
    FOREIGN KEY (folder_id) REFERENCES metadata_database.middleware.folder(folder_id)
);

CREATE TABLE IF NOT EXISTS metadata_database.middleware.file(
    file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folder_id UUID NOT NULL,
    file_type_id UUID NOT NULL,

    name VARCHAR(50) NOT NULL,
    size DECIMAL NOT NULL,
    date_created DATE NOT NULL,
    date_modified DATE NOT NULL,
    total_partitions INTEGER,

    FOREIGN KEY (folder_id) REFERENCES metadata_database.middleware.folder(folder_id),
    FOREIGN KEY (file_type_id) REFERENCES metadata_database.middleware.file_type(file_type_id)
);


CREATE TABLE IF NOT EXISTS metadata_database.middleware.node(
    node_id UUID NOT NULL DEFAULT gen_random_uuid(),
    status_id UUID NOT NULL,

    port INTEGER NOT NULL,
    url TEXT NOT NULL,
    max_disk_size DECIMAL NOT NULL,
    free_disk_size DECIMAL NOT NULL,
    last_heatbeat timestamp NOT NULL,

    FOREIGN KEY (status_id) REFERENCES metadata_database.middleware.status(status_id)
);

CREATE TABLE IF NOT EXISTS metadata_database.middleware.chunk(
    chunk_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID NOT NULL,
    node_id UUID NOT NULL,

    partition_num INTEGER NOT NULL,
    is_replica BOOLEAN NOT NULL ,
    size DECIMAL NOT NULL,
    path TEXT NOT NULL,
    version VARCHAR(10),

    FOREIGN KEY (file_id) REFERENCES metadata_database.middleware.file(file_id),
    FOREIGN KEY (node_id) REFERENCES metadata_database.middleware.node(node_id)
);


-- =============================
-- === Procesos almacenados ====
-- =============================

