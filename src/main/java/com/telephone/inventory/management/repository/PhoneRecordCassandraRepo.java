package com.telephone.inventory.management.repository;

import com.common.models.enums.PhoneNumberStatus;
import com.common.models.model.PhoneRecordCassandra;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneRecordCassandraRepo extends CassandraRepository<PhoneRecordCassandra, String> {

    @Query("UPDATE phone_records " +
            "SET correlationid = :correlationId, status = :newStatus, version = :newVersion, userid = :userId " +
            "WHERE e164number = :e164Number " +
            "IF version = :currentVersion AND status = :expectedStatus")
    boolean updateIfMatch(String correlationId,
                          PhoneNumberStatus newStatus,
                          int newVersion,
                          String userId,
                          String e164Number,
                          int currentVersion,
                          PhoneNumberStatus expectedStatus);

}

