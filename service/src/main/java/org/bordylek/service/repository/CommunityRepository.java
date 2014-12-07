package org.bordylek.service.repository;

import org.bordylek.service.model.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public abstract interface CommunityRepository extends MongoRepository<Community, String> {
	Page<Community> findByLocationNear(Point p, Distance d, Pageable page);
}
