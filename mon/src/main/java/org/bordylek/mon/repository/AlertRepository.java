package org.bordylek.mon.repository;

import org.bordylek.mon.model.Alert;
import org.bordylek.mon.model.Severity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {

    List<Alert> findByOrderByTimestampDesc();
    List<Alert> findByResolvedOrderByTimestampDesc(boolean resolved);
    List<Alert> findByResolvedAndTimestampBetweenOrderByTimestampDesc(boolean resolved, Date from, Date to);
    List<Alert> findByFqNameAndTimestampAndSeverity(String fqName, Date date, Severity severity);
    List<Alert> findByFqNameAndTimestampLessThanAndSeverity(String fqName, Date date, Severity severity);
}
