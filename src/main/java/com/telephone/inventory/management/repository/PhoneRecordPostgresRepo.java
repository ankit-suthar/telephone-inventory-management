package com.telephone.inventory.management.repository;

import com.common.models.enums.PhoneNumberStatus;
import com.common.models.model.PhoneRecordPostgres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PhoneRecordPostgresRepo extends JpaRepository<PhoneRecordPostgres, String> {

    // Custom update with WHERE clause for conditional match
    @Modifying
    @Transactional
    @Query("UPDATE PhoneRecordPostgres p " +
            "SET p.correlationId = :correlationId, " +
            "    p.status = :newStatus, " +
            "    p.version = :newVersion, " +
            "    p.userId = :userId " +
            "WHERE p.e164Number = :e164Number " +
            "  AND p.version = :currentVersion " +
            "  AND p.status = :expectedStatus")
    int updateIfMatch(@Param("correlationId") String correlationId,
                      @Param("newStatus") PhoneNumberStatus newStatus,
                      @Param("newVersion") int newVersion,
                      @Param("userId") String userId,
                      @Param("e164Number") String e164Number,
                      @Param("currentVersion") int currentVersion,
                      @Param("expectedStatus") PhoneNumberStatus expectedStatus);
}
