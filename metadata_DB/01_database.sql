-- ======================================
-- === Creacion de Usuario Middleware ===
-- ======================================

-- Creacion del esquema
CREATE SCHEMA IF NOT EXISTS middleware;

-- Creacion del usuario (Login)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'middleware_admin') THEN
        CREATE USER middleware_admin WITH PASSWORD 'middlewarepassword';
    END IF;
END
$$;

-- Permiso para el usuario "middleware_admin" para entrar a la base de datos
GRANT CONNECT ON DATABASE metadata_database TO middleware_admin;

-- Permiso para entrar al esquema
GRANT USAGE, CREATE ON SCHEMA middleware TO middleware_admin;

-- Privilegios minimos operativos para tablas ACTUALES (CRUD: Select, Insert, Update, Delete)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA middleware TO middleware_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA middleware TO middleware_admin;
GRANT EXECUTE ON ALL PROCEDURES IN SCHEMA middleware TO middleware_admin;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA middleware TO middleware_admin;

-- Privilegios por defecto para tablas FUTURAS
ALTER DEFAULT PRIVILEGES IN SCHEMA middleware
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO middleware_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA middleware
    GRANT USAGE, SELECT ON SEQUENCES TO middleware_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA middleware
    GRANT EXECUTE ON ROUTINES TO middleware_admin;

-- Configurar search_path por defecto
ALTER ROLE middleware_admin SET search_path TO middleware, public;
ALTER DATABASE metadata_database SET search_path TO middleware, public;
