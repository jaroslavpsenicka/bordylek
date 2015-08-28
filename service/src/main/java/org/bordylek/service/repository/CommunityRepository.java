package org.bordylek.service.repository;

import org.bordylek.service.model.Community;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends MongoRepository<Community, String> {

	@Query("{'location': {'$nearSphere': {'$geometry': {'x': ?1, 'y': ?0}, '$maxDistance': ?2}}}")
	Page<Community> findByLocationNear(double lat, double lng, double dist, Pageable page);

	Community findByTitle(String title);

	@Cacheable(value = "comms")
	Community findOne(String id);
}
