-- ===============================
-- === Creacion de las tablas ====
-- ===============================

-- Tablas independientes

CREATE TABLE IF NOT EXISTS middleware.user(
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username VARCHAR(30) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    date_created TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS middleware.file_type(
    file_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(20) NOT NULL,
    description VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS middleware.status(
    status_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(50) NOT NULL
    );

-- Tablas cruzadas

CREATE TABLE IF NOT EXISTS middleware.folder(
    folder_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    dad_folder_id UUID,

    name VARCHAR(50) NOT NULL,
    date_created TIMESTAMP NOT NULL,

    FOREIGN KEY (user_id) REFERENCES middleware.user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (dad_folder_id) REFERENCES middleware.folder(folder_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS middleware.file(
    file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folder_id UUID NOT NULL,
    file_type_id UUID NOT NULL,

    name VARCHAR(50) NOT NULL,
    size BIGINT NOT NULL,
    date_created TIMESTAMP NOT NULL,
    date_modified TIMESTAMP NOT NULL,
    total_partitions INTEGER NOT NULL,

    FOREIGN KEY (folder_id) REFERENCES middleware.folder(folder_id) ON DELETE CASCADE,
    FOREIGN KEY (file_type_id) REFERENCES middleware.file_type(file_type_id)
    );

CREATE TABLE IF NOT EXISTS middleware.node(
    node_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    region_name VARCHAR(50) NOT NULL,
    max_disk_size BIGINT NOT NULL,
    free_disk_size BIGINT NOT NULL,
    last_heatbeat TIMESTAMP NOT NULL,

    FOREIGN KEY (status_id) REFERENCES middleware.status(status_id)
    );

CREATE TABLE IF NOT EXISTS middleware.chunk(
    chunk_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID NOT NULL,
    node_id UUID NOT NULL,
    part_number INTEGER NOT NULL,
    is_replica BOOLEAN NOT NULL,
    bytes_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    file_key VARCHAR(116) NOT NULL,
    version VARCHAR(10),

    FOREIGN KEY (file_id) REFERENCES middleware.file(file_id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES middleware.node(node_id)
    );


-- Datos semilla para status
INSERT INTO middleware.status (status_id, name, description)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ONLINE', 'Nodo activo y respondiendo'),
    ('22222222-2222-2222-2222-222222222222', 'OFFLINE', 'Nodo desconectado o inactivo'),
    ('33333333-3333-3333-3333-333333333333', 'CORRUPTED', 'Nodo con almacenamiento corrupto')
    ON CONFLICT (status_id) DO NOTHING;

-- Datos semilla para file_type
INSERT INTO middleware.file_type (file_type_id, name, description) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'doc', 'documentos de word'),
    ('e0000000-0000-0000-0000-000000000002', 'pdf', 'formato de documento portátil'),
    ('e0000000-0000-0000-0000-000000000003', 'png', 'Gráficos de Red Portátiles'),
    ('e0000000-0000-0000-0000-000000000004', 'jpg', 'Joint Photographic Experts Group'),
    ('e0000000-0000-0000-0000-000000000005', 'mp3', 'MPEG-1 Audio Layer III'),
    ('e0000000-0000-0000-0000-000000000006', 'mp4', 'MPEG-4 Part 14')
    ON CONFLICT (file_type_id) DO NOTHING;

-- Datos semilla para nodos de almacenamiento (requeridos para partición y asignación)
INSERT INTO middleware.node (node_id, status_id, name, region_name, max_disk_size, free_disk_size, last_heatbeat) VALUES
    ('a1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'nodo-a', 'us-east-1', 10737418240, 10737418240, CURRENT_TIMESTAMP),
    ('a2222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'nodo-b', 'us-east-1', 10737418240, 10737418240, CURRENT_TIMESTAMP),
    ('a3333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'nodo-c', 'us-east-1', 10737418240, 10737418240, CURRENT_TIMESTAMP)
    ON CONFLICT (name) DO NOTHING;
