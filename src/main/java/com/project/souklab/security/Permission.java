package com.project.souklab.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Canonical permission contract. Each permission is represented by a grouped
 * enum value such as {@code Permission.Admin.USERS}; no flat permission
 * constants are exposed.
 */
public interface Permission {
    String value();

    default boolean matches(GrantedAuthority grantedAuthority) {
        return grantedAuthority != null && value().equals(grantedAuthority.getAuthority());
    }

    default boolean matches(String permissionKey) {
        return permissionKey != null && value().equals(permissionKey);
    }

    default String description() {
        return value();
    }

    /** Complete permission catalog used by reference-data seeding and tooling. */
    static List<Permission> all() {
        return List.of(
                Admin.USERS, Admin.FORMATIONS, Admin.FEED, Admin.REPORTS,
                Financial.ADMIN,
                Artisan.FORMATIONS, Artisan.CONTENT, Artisan.REVIEWS,
                Profile.READ, Profile.WRITE,
                Report.CREATE, File.READ, Message.SEND,
                Analytics.ADMIN);
    }

    enum Admin implements Permission {
        USERS("permission:admin:users"), FORMATIONS("permission:admin:formations"),
        FEED("permission:admin:feed"), REPORTS("permission:admin:reports");
        private final String value;
        Admin(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum Artisan implements Permission {
        FORMATIONS("permission:artisan:formations"), CONTENT("permission:artisan:content"),
        REVIEWS("permission:artisan:reviews");
        private final String value;
        Artisan(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum Profile implements Permission {
        READ("permission:profile:read"), WRITE("permission:profile:write");
        private final String value;
        Profile(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum Report implements Permission {
        CREATE("permission:report:create");
        private final String value;
        Report(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum File implements Permission {
        READ("permission:file:read");
        private final String value;
        File(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum Message implements Permission {
        SEND("permission:message:send");
        private final String value;
        Message(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum Analytics implements Permission {
        ADMIN("permission:analytics:admin");
        private final String value;
        Analytics(String value) { this.value = value; }
        public String value() { return value; }
    }

    enum Financial implements Permission {
        ADMIN("permission:financial:admin");
        private final String value;
        Financial(String value) { this.value = value; }
        public String value() { return value; }
    }
}
