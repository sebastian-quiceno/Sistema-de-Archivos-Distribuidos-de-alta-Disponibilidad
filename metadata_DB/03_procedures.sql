-- =============================
-- === Procesos almacenados ====
-- =============================

--- Procesos y funciones de User ---

CREATE OR REPLACE PROCEDURE middleware.sp_save_user(
    p_user_id UUID,
    p_username VARCHAR(30),
    p_password_hash TEXT,
    p_date_created TIMESTAMP
)
    LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO middleware.user (user_id, username, password_hash, date_created)
    VALUES (p_user_id, p_username, p_password_hash, p_date_created)
    ON CONFLICT (user_id) DO UPDATE
    SET username = EXCLUDED.username,
        password_hash = EXCLUDED.password_hash,
        date_created = EXCLUDED.date_created;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_user_by_id(p_user_id UUID)
    RETURNS TABLE (
      user_id UUID,
      username VARCHAR(30),
      password_hash TEXT,
      date_created TIMESTAMP
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT u.user_id, u.username, u.password_hash, u.date_created
        FROM middleware.user u
        WHERE u.user_id = p_user_id;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_user_by_username(p_username VARCHAR(30))
    RETURNS TABLE (
      user_id UUID,
      username VARCHAR(30),
      password_hash TEXT,
      date_created TIMESTAMP
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT u.user_id, u.username, u.password_hash, u.date_created
        FROM middleware.user u
        WHERE u.username = p_username;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_exist_by_username(p_username VARCHAR(30))
    RETURNS BOOLEAN
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM middleware.user u
        WHERE u.username = p_username
    );
END;
$$;

CREATE OR REPLACE PROCEDURE middleware.sp_delete_user_by_id(p_user_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM middleware.user
    WHERE user_id = p_user_id;
END;
$$;


--- Procesos y funciones de FileType ---

CREATE OR REPLACE PROCEDURE middleware.sp_save_file_type(
    p_file_type_id UUID,
    p_name VARCHAR(20),
    p_description VARCHAR(50)
)
    LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO middleware.file_type (file_type_id, name, description)
    VALUES (p_file_type_id, p_name, p_description)
    ON CONFLICT (file_type_id) DO UPDATE
        SET name = EXCLUDED.name,
            description = EXCLUDED.description;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_file_type_by_id(p_file_type_id UUID)
    RETURNS TABLE (
      file_type_id UUID,
      name VARCHAR(20),
      description VARCHAR(50)
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT ft.file_type_id, ft.name, ft.description
        FROM middleware.file_type ft
        WHERE ft.file_type_id = p_file_type_id;
END;
$$;

CREATE OR REPLACE PROCEDURE middleware.sp_delete_file_type_by_id(p_file_type_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM middleware.file_type
    WHERE file_type_id = p_file_type_id;
END;
$$;


--- Procesos y funcinoes de Node ---

CREATE OR REPLACE PROCEDURE middleware.sp_save_node(
    p_node_id UUID,
    p_status_id UUID,
    p_name VARCHAR(50),
    p_region_name VARCHAR(50),
    p_max_disk_size BIGINT,
    p_free_disk_size BIGINT,
    p_last_heatbeat TIMESTAMP
)
    LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO middleware.node (node_id, status_id, name, region_name, max_disk_size, free_disk_size, last_heatbeat)
    VALUES (p_node_id, p_status_id, p_name, p_region_name, p_max_disk_size, p_free_disk_size, p_last_heatbeat)
    ON CONFLICT (node_id) DO UPDATE
    SET status_id = EXCLUDED.status_id,
        name = EXCLUDED.name,
        region_name = EXCLUDED.region_name,
        max_disk_size = EXCLUDED.max_disk_size,
        free_disk_size = EXCLUDED.free_disk_size,
        last_heatbeat = EXCLUDED.last_heatbeat;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_node_by_id(p_node_id UUID)
    RETURNS TABLE (
      node_id UUID,
      status_id UUID,
      status_name VARCHAR(20),
      name VARCHAR(50),
      region_name VARCHAR(50),
      max_disk_size BIGINT,
      free_disk_size BIGINT,
      last_heatbeat TIMESTAMP
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT n.node_id, n.status_id, s.name AS status_name, n.name, n.region_name, n.max_disk_size, n.free_disk_size, n.last_heatbeat
        FROM middleware.node n
                 JOIN middleware.status s ON n.status_id = s.status_id
        WHERE n.node_id = p_node_id;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_online_node_ids()
    RETURNS TABLE (node_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT n.node_id
        FROM middleware.node n
                 JOIN middleware.status s ON n.status_id = s.status_id
        WHERE UPPER(s.name) = 'ONLINE';
END;
$$;

CREATE OR REPLACE PROCEDURE middleware.sp_delete_node_by_id(p_node_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM middleware.node
    WHERE node_id = p_node_id;
END;
$$;


--- Procesos y funcinoes de Folder ---

CREATE OR REPLACE PROCEDURE middleware.sp_save_folder(
    p_folder_id UUID,
    p_user_id UUID,
    p_dad_folder_id UUID,
    p_name VARCHAR(50),
    p_date_created TIMESTAMP
)
    LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO middleware.folder (folder_id, user_id, dad_folder_id, name, date_created)
    VALUES (p_folder_id, p_user_id, p_dad_folder_id, p_name, p_date_created)
    ON CONFLICT (folder_id) DO UPDATE
    SET user_id = EXCLUDED.user_id,
        dad_folder_id = EXCLUDED.dad_folder_id,
        name = EXCLUDED.name,
        date_created = EXCLUDED.date_created;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_folder_by_id(p_folder_id UUID)
    RETURNS TABLE (
      folder_id UUID,
      user_id UUID,
      dad_folder_id UUID,
      name VARCHAR(50),
      date_created TIMESTAMP
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT f.folder_id, f.user_id, f.dad_folder_id, f.name, f.date_created
        FROM middleware.folder f
        WHERE f.folder_id = p_folder_id;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_related_folders(p_folder_id UUID)
    RETURNS TABLE (
      folder_id UUID,
      user_id UUID,
      dad_folder_id UUID,
      name VARCHAR(50),
      date_created TIMESTAMP
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT f.folder_id, f.user_id, f.dad_folder_id, f.name, f.date_created
        FROM middleware.folder f
        WHERE f.dad_folder_id = p_folder_id;
END;
$$;

CREATE OR REPLACE PROCEDURE middleware.sp_delete_folder_by_id(p_folder_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM middleware.folder
    WHERE folder_id = p_folder_id;
END;
$$;


--- Procesos y funcinoes de File ---

CREATE OR REPLACE PROCEDURE middleware.sp_save_file(
    p_file_id UUID,
    p_folder_id UUID,
    p_file_type_id UUID,
    p_name VARCHAR(50),
    p_size BIGINT,
    p_date_created TIMESTAMP,
    p_date_modified TIMESTAMP,
    p_total_partitions INTEGER
)
    LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO middleware.file (file_id, folder_id, file_type_id, name, size, date_created, date_modified, total_partitions)
    VALUES (p_file_id, p_folder_id, p_file_type_id, p_name, p_size, p_date_created, p_date_modified, p_total_partitions)
    ON CONFLICT (file_id) DO UPDATE
    SET folder_id = EXCLUDED.folder_id,
        file_type_id = EXCLUDED.file_type_id,
        name = EXCLUDED.name,
        size = EXCLUDED.size,
        date_created = EXCLUDED.date_created,
        date_modified = EXCLUDED.date_modified,
        total_partitions = EXCLUDED.total_partitions;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_file_by_id(p_file_id UUID)
    RETURNS TABLE (
      file_id UUID,
      folder_id UUID,
      file_type_id UUID,
      name VARCHAR(50),
      size BIGINT,
      date_created TIMESTAMP,
      date_modified TIMESTAMP,
      total_partitions INTEGER
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT f.file_id, f.folder_id, f.file_type_id, f.name, f.size, f.date_created, f.date_modified, f.total_partitions
        FROM middleware.file f
        WHERE f.file_id = p_file_id;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_all_files()
    RETURNS TABLE (
      file_id UUID,
      folder_id UUID,
      file_type_id UUID,
      name VARCHAR(50),
      size BIGINT,
      date_created TIMESTAMP,
      date_modified TIMESTAMP,
      total_partitions INTEGER
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT f.file_id, f.folder_id, f.file_type_id, f.name, f.size, f.date_created, f.date_modified, f.total_partitions
        FROM middleware.file f;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_find_files_page(p_limit INTEGER, p_offset INTEGER)
    RETURNS TABLE (
      file_id UUID,
      folder_id UUID,
      file_type_id UUID,
      name VARCHAR(50),
      size BIGINT,
      date_created TIMESTAMP,
      date_modified TIMESTAMP,
      total_partitions INTEGER
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT f.file_id, f.folder_id, f.file_type_id, f.name, f.size, f.date_created, f.date_modified, f.total_partitions
        FROM middleware.file f
        ORDER BY f.date_created DESC
        LIMIT p_limit OFFSET p_offset;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_original_chunks_by_file_id(p_file_id UUID)
    RETURNS TABLE (
      chunk_id UUID,
      file_id UUID,
      node_id UUID,
      part_number INTEGER,
      is_replica BOOLEAN,
      bytes_size BIGINT,
      file_hash VARCHAR(64),
      file_key VARCHAR(116),
      version VARCHAR(10)
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT c.chunk_id, c.file_id, c.node_id, c.part_number, c.is_replica, c.bytes_size, c.file_hash, c.file_key, c.version
        FROM middleware.chunk c
        WHERE c.file_id = p_file_id AND c.is_replica = FALSE
        ORDER BY c.part_number ASC;
END;
$$;

CREATE OR REPLACE PROCEDURE middleware.sp_delete_file_by_id(p_file_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM middleware.file
    WHERE file_id = p_file_id;
END;
$$;


--- Procesos y funcinoes de Chunk ---

CREATE OR REPLACE PROCEDURE middleware.sp_save_chunk(
    p_chunk_id UUID,
    p_file_id UUID,
    p_node_id UUID,
    p_part_number INTEGER,
    p_is_replica BOOLEAN,
    p_bytes_size BIGINT,
    p_file_hash VARCHAR(64),
    p_file_key VARCHAR(116),
    p_version VARCHAR(10)
)
    LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO middleware.chunk (chunk_id, file_id, node_id, part_number, is_replica, bytes_size, file_hash, file_key, version)
    VALUES (p_chunk_id, p_file_id, p_node_id, p_part_number, p_is_replica, p_bytes_size, p_file_hash, p_file_key, p_version)
    ON CONFLICT (chunk_id) DO UPDATE
    SET file_id = EXCLUDED.file_id,
        node_id = EXCLUDED.node_id,
        part_number = EXCLUDED.part_number,
        is_replica = EXCLUDED.is_replica,
        bytes_size = EXCLUDED.bytes_size,
        file_hash = EXCLUDED.file_hash,
        file_key = EXCLUDED.file_key,
        version = EXCLUDED.version;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_chunk_by_id(p_chunk_id UUID)
    RETURNS TABLE (
      chunk_id UUID,
      file_id UUID,
      node_id UUID,
      part_number INTEGER,
      is_replica BOOLEAN,
      bytes_size BIGINT,
      file_hash VARCHAR(64),
      file_key VARCHAR(116),
      version VARCHAR(10)
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT c.chunk_id, c.file_id, c.node_id, c.part_number, c.is_replica, c.bytes_size, c.file_hash, c.file_key, c.version
        FROM middleware.chunk c
        WHERE c.chunk_id = p_chunk_id;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_all_chunks()
    RETURNS TABLE (
      chunk_id UUID,
      file_id UUID,
      node_id UUID,
      part_number INTEGER,
      is_replica BOOLEAN,
      bytes_size BIGINT,
      file_hash VARCHAR(64),
      file_key VARCHAR(116),
      version VARCHAR(10)
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT c.chunk_id, c.file_id, c.node_id, c.part_number, c.is_replica, c.bytes_size, c.file_hash, c.file_key, c.version
        FROM middleware.chunk c;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_chunk_file_hash(p_chunk_id UUID)
    RETURNS VARCHAR(64)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_hash VARCHAR(64);
BEGIN
    SELECT c.file_hash INTO v_hash
    FROM middleware.chunk c
    WHERE c.chunk_id = p_chunk_id;
    RETURN v_hash;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_chunks_by_file_id(p_file_id UUID)
    RETURNS TABLE (
      chunk_id UUID,
      file_id UUID,
      node_id UUID,
      part_number INTEGER,
      is_replica BOOLEAN,
      bytes_size BIGINT,
      file_hash VARCHAR(64),
      file_key VARCHAR(116),
      version VARCHAR(10)
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT c.chunk_id, c.file_id, c.node_id, c.part_number, c.is_replica, c.bytes_size, c.file_hash, c.file_key, c.version
        FROM middleware.chunk c
        WHERE c.file_id = p_file_id AND c.is_replica = FALSE
        ORDER BY c.part_number ASC;
END;
$$;

CREATE OR REPLACE FUNCTION middleware.sp_get_replica_chunks_by_chunk_id(p_chunk_id UUID)
    RETURNS TABLE (
      chunk_id UUID,
      file_id UUID,
      node_id UUID,
      part_number INTEGER,
      is_replica BOOLEAN,
      bytes_size BIGINT,
      file_hash VARCHAR(64),
      file_key VARCHAR(116),
      version VARCHAR(10)
                  )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_file_id UUID;
    v_file_key VARCHAR(116);
    v_part_number INTEGER;
BEGIN
    SELECT c.file_id, c.file_key, c.part_number
    INTO v_file_id, v_file_key, v_part_number
    FROM middleware.chunk c
    WHERE c.chunk_id = p_chunk_id;

    RETURN QUERY
        SELECT c.chunk_id, c.file_id, c.node_id, c.part_number, c.is_replica, c.bytes_size, c.file_hash, c.file_key, c.version
        FROM middleware.chunk c
        WHERE c.file_id = v_file_id
          AND (c.file_key = v_file_key OR c.part_number = v_part_number)
          AND c.is_replica = TRUE;
END;
$$;

CREATE OR REPLACE PROCEDURE middleware.sp_delete_chunk_by_id(p_chunk_id UUID)
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM middleware.chunk
    WHERE chunk_id = p_chunk_id;
END;
$$;
