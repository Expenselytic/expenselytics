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

    @Modifying
    @Query("delete from UserSessionEntity s where s.tokenHash = :hash")
    void deleteToken(@Param("hash") String hash);

    @Query(
        "select s.tokenHash from UserSessionEntity s where " +
            "s.expiresAt < :now order by s.expiresAt"
    )
    java.util.List<String> findExpiredIds(
        @Param("now") Instant now,
        org.springframework.data.domain.Pageable page
    );
}
