package org.bordylek.mon.repository;

import org.bordylek.mon.model.Chart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChartRepository extends MongoRepository<Chart, String> {

}
