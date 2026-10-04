package com.expenlytics.db.repository;

import com.expenlytics.db.entity.UserDeviceInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceInfoRepository
    extends JpaRepository<UserDeviceInfoEntity, Long>
{
    @org.springframework.data.jpa.repository.Query(
        "select d.id from UserDeviceInfoEntity d where d.createdAt < " +
            ":cutoff order by d.createdAt"
    )
    java.util.List<Long> findOldIds(
        @org.springframework.data.repository.query.Param(
            "cutoff"
        ) java.time.Instant cutoff,
        org.springframework.data.domain.Pageable page
    );
}
