package com.sdan.middleware.Application.Ports.Security;

import java.util.List;

public final class Permissions {

    private Permissions() {
    }

    public static final class Files {
        public static final String READ = "files.read";
        public static final String CREATE = "files.create";
        public static final String UPDATE = "files.update";
        public static final String DELETE = "files.delete";

        private Files() {}
    }

    public static final class Folders {
        public static final String READ = "folders.read";
        public static final String CREATE = "folders.create";
        public static final String UPDATE = "folders.update";
        public static final String DELETE = "folders.delete";

        private Folders() {}
    }

    public static final class Users {
        public static final String READ = "users.read";
        public static final String CREATE = "users.create";
        public static final String UPDATE = "users.update";
        public static final String DELETE = "users.delete";

        private Users() {}
    }

    public static final class Nodes {
        public static final String READ = "nodes.read";
        public static final String CREATE = "nodes.create";
        public static final String UPDATE = "nodes.update";
        public static final String DELETE = "nodes.delete";

        private Nodes() {}
    }

    public static List<String> defaultUserPermissions() {
        return List.of(
                Files.READ, Files.CREATE, Files.UPDATE, Files.DELETE,
                Folders.READ, Folders.CREATE, Folders.UPDATE, Folders.DELETE,
                Users.READ
        );
    }
}
