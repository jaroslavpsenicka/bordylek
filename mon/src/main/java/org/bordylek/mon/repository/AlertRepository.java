package org.bordylek.mon.repository;

import org.bordylek.mon.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {

    List<Alert> findByOrderByTimestampDesc();
    List<Alert> findByResolvedOrderByTimestampDesc(boolean resolved);

}
