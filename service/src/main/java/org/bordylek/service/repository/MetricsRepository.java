package org.bordylek.service.repository;

import org.bordylek.service.model.Metrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MetricsRepository extends MongoRepository<Metrics, String> {

    List<Metrics> findByName(String name);

    Metrics findTopByOrderByTimestampDesc();

    List<Metrics> findByTimestamp(Date timestamp);

    @Query("{ _class: ?0, timestamp: ?1 }")
    List<Metrics> findAllOfTypeAndTimestamp(String className, Date timestamp);

    @Query("{ _class: ?0, name: ?1, timestamp: { $gte: ?2 }}")
    List<Metrics> findAllOfTypeAndNameNewerThan(String className, String name, Date date);
}
