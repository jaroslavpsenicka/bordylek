package org.bordylek.service.repository;

import org.bordylek.service.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<Log, String> {
    List<Log> findByTimestamp(Date timestamp);

    Iterable<Log> findByTimestampLessThan(Date date);
}
