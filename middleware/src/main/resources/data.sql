-- ==========================================
-- Valores de prueba para entorno local / H2
-- ==========================================

-- ================
-- === Usuario ===
-- ================
-- Nota: La tabla se llama "user" (entre comillas por ser palabra reservada en SQL)
-- Columnas mapeadas: user_id, username, password_hash, date_created
INSERT INTO "user" (user_id, username, password_hash, date_created) VALUES
    ('afca2800-af2a-4251-93f7-c90dcaabea48', 'sebas', '$2a$10$ehTty9fFux.0EtEdYwVZou/OES6pJk67R8PjDI4e5WUpwHK5me4CK', CURRENT_TIMESTAMP);

-- ============================
-- === Estados de los Nodos ===
-- ============================
-- Requerido por la foreign key de la tabla 'node'
INSERT INTO status (status_id, name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ONLINE', 'Nodo activo y respondiendo'),
    ('22222222-2222-2222-2222-222222222222', 'OFFLINE', 'Nodo desconectado o inactivo'),
    ('33333333-3333-3333-3333-333333333333', 'CORRUPTED', 'Nodo con almacenamiento corrupto');

-- =========================
-- === Tipos de archivos ===
-- =========================
-- Requiere file_type_id explícito ya que la entidad no tiene generación automática
INSERT INTO file_type (file_type_id, name, description) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'doc', 'documentos de word'),
    ('e0000000-0000-0000-0000-000000000002', 'pdf', 'formato de documento portátil'),
    ('e0000000-0000-0000-0000-000000000003', 'png', 'Gráficos de Red Portátiles'),
    ('e0000000-0000-0000-0000-000000000004', 'jpg', 'Joint Photographic Experts Group'),
    ('e0000000-0000-0000-0000-000000000005', 'mp3', 'MPEG-1 Audio Layer III'),
    ('e0000000-0000-0000-0000-000000000006', 'mp4', 'MPEG-4 Part 14');

-- ================
-- === Carpetas ===
-- ================
-- La tabla destino es 'folder'. Cada fila debe tener su propio 'folder_id' único.
-- Carpeta raíz (dad_folder_id = null):
INSERT INTO folder (folder_id, user_id, dad_folder_id, name, date_created) VALUES
    ('0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1', 'afca2800-af2a-4251-93f7-c90dcaabea48', NULL, 'root', CURRENT_TIMESTAMP);

-- Subcarpetas hijas de 'primera carpeta' (dad_folder_id = '0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1'):
INSERT INTO folder (folder_id, user_id, dad_folder_id, name, date_created) VALUES
    ('1541ed6f-6ee5-4cb2-aa75-e3d4d74d75b2', 'afca2800-af2a-4251-93f7-c90dcaabea48', '0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1', 'subcarpeta 1', CURRENT_TIMESTAMP),
    ('2541ed6f-6ee5-4cb2-aa75-e3d4d74d75b3', 'afca2800-af2a-4251-93f7-c90dcaabea48', '0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1', 'subcarpeta 2', CURRENT_TIMESTAMP);

-- Otra carpeta raíz adicional para pruebas:
INSERT INTO folder (folder_id, user_id, dad_folder_id, name, date_created) VALUES
    ('3541ed6f-6ee5-4cb2-aa75-e3d4d74d75b4', 'afca2800-af2a-4251-93f7-c90dcaabea48', NULL, 'segunda carpeta raiz', CURRENT_TIMESTAMP);

-- ===============================
-- === Nodos de almacenamiento ===
-- ===============================
-- Nodos ONLINE para que el servicio de partición y asignación pueda operar
INSERT INTO node (node_id, status_id, name, region_name, max_disk_size, free_disk_size, last_heatbeat) VALUES
    ('a1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'nodo-a', 'us-east-1', 10737418240, 10737418240, CURRENT_TIMESTAMP),
    ('a2222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'nodo-b', 'us-east-1', 10737418240, 10737418240, CURRENT_TIMESTAMP),
    ('a3333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'nodo-c', 'us-east-1', 10737418240, 10737418240, CURRENT_TIMESTAMP);

-- ================
-- === Archivos ===
-- ================
INSERT INTO file (file_id, folder_id, file_type_id, name, size, date_created, date_modified, total_partitions) VALUES
    ('e1000000-0000-0000-0000-000000000001','0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1', 'e0000000-0000-0000-0000-000000000001', 'Prueba DOC', 31457288, CURRENT_DATE, CURRENT_DATE, 3),
    ('e2000000-0000-0000-0000-000000000001', '0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1', 'e0000000-0000-0000-0000-000000000002', 'Prueba PDF', 31457288, CURRENT_DATE, CURRENT_DATE, 2),
    ('e3000000-0000-0000-0000-000000000001', '0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1', 'e0000000-0000-0000-0000-000000000005', 'Prueba MP3', 31457288, CURRENT_DATE, CURRENT_DATE, 2);

