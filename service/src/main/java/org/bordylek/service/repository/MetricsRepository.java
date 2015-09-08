package org.bordylek.service.repository;

import org.bordylek.service.model.AbstractMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MetricsRepository extends MongoRepository<AbstractMetric, String> {

    List<AbstractMetric> findByName(String name);

    AbstractMetric findTopByOrderByTimestampDesc();

    List<AbstractMetric> findByTimestamp(Date timestamp);
}
