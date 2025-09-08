package com.telephone.inventory.management.repository;

import com.common.models.enums.PhoneNumberStatus;
import com.common.models.model.PhoneRecordCassandra;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneRecordCassandraRepo extends CassandraRepository<PhoneRecordCassandra, String> {

    @Query("UPDATE phone_record SET correlation_id = ?1 status = ?2, version = ?3 WHERE e164_number = ?4 IF version = ?5 AND status = ?6")
    boolean updateIfMatch(String correlationId, PhoneNumberStatus newStatus, int newVersion, String e164Number, int currentVersion, PhoneNumberStatus expectedStatus);
}

