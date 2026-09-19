package com.project.souklab.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Canonical permission contract. Each permission is represented by a grouped
 * enum value such as {@code Permission.Admin.USERS}; no flat permission
 * constants are exposed.
 */
public interface Permission {
    String authority();

    default boolean matches(GrantedAuthority grantedAuthority) {
        return grantedAuthority != null && authority().equals(grantedAuthority.getAuthority());
    }

    default boolean matches(String permissionKey) {
        return permissionKey != null && authority().equals(permissionKey);
    }

    default String description() {
        return authority();
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
        private final String authority;
        Admin(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum Artisan implements Permission {
        FORMATIONS("permission:artisan:formations"), CONTENT("permission:artisan:content"),
        REVIEWS("permission:artisan:reviews");
        private final String authority;
        Artisan(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum Profile implements Permission {
        READ("permission:profile:read"), WRITE("permission:profile:write");
        private final String authority;
        Profile(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum Report implements Permission {
        CREATE("permission:report:create");
        private final String authority;
        Report(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum File implements Permission {
        READ("permission:file:read");
        private final String authority;
        File(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum Message implements Permission {
        SEND("permission:message:send");
        private final String authority;
        Message(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum Analytics implements Permission {
        ADMIN("permission:analytics:admin");
        private final String authority;
        Analytics(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }

    enum Financial implements Permission {
        ADMIN("permission:financial:admin");
        private final String authority;
        Financial(String authority) { this.authority = authority; }
        public String authority() { return authority; }
    }
}
