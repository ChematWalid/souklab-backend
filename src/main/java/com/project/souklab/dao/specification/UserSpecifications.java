package com.project.souklab.dao.specification;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable, composable JPA specifications for dynamic {@link User} queries.
 * Enables combining search filters, account statuses, and soft-delete criteria
 * without generating rigid, exploding repository query method permutations.
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    /**
     * Filters records that have not been soft-deleted.
     */
    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    /**
     * Filters users matching a specific account status.
     *
     * @param status target status, or null for any status
     */
    public static Specification<User> hasStatus(AccountStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Case-insensitive substring match across email, firstName, and lastName.
     *
     * @param keyword search keyword, or null/blank for no filter
     */
    public static Specification<User> searchKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }
}
