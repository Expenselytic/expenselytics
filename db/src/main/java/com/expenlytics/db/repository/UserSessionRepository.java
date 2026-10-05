package com.expenlytics.db.repository;

import com.expenlytics.core.model.AccountProfile;
import com.expenlytics.db.entity.UserSessionEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UserSessionRepository
    extends JpaRepository<UserSessionEntity, String>
{
    @Query(
        "select new com.expenlytics.core.model.AccountProfile(u.id, " +
            "u.name, u.email, u.createdAt) " +
            "from UserSessionEntity s join s.user u where s.tokenHash " +
            "= :hash and s.expiresAt > :now"
    )
    Optional<AccountProfile> findProfile(
        @Param("hash") String hash,
        @Param("now") Instant now
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update UserEntity u set u.lastLogin = " +
            ":endedAt where u.id = :userId " +
            "and (u.lastLogin is null or u.lastLogin < :endedAt)"
    )
    void recordEnd(
        @Param("userId") Long userId,
        @Param("endedAt") Instant endedAt
    );

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select s from UserSessionEntity s join " +
            "fetch s.user where s.tokenHash = :hash"
    )
    Optional<UserSessionEntity> findForEnd(@Param("hash") String hash);

    default void endSession(String hash, Instant now) {
        findForEnd(hash).ifPresent(session -> {
            var endedAt = session.expiresAt.isBefore(now)
                ? session.expiresAt
                : now;
            recordEnd(session.user.id, endedAt);
            deleteToken(hash);
        });
    }

    @Modifying
    @Query("delete from UserSessionEntity s where s.tokenHash = :hash")
    void deleteToken(@Param("hash") String hash);

    @Query(
        "select s.tokenHash from UserSessionEntity s where " +
            "s.expiresAt <= :now order by s.expiresAt"
    )
    java.util.List<String> findExpiredIds(
        @Param("now") Instant now,
        org.springframework.data.domain.Pageable page
    );
}
