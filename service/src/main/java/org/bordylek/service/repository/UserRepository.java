package org.bordylek.service.repository;

import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	@Cacheable("users")
	User findByRegId(String regId);

	@Cacheable("users")
	User findByEmail(String email);

	@Cacheable(value = "users")
	User findOne(String id);

	@Override
	@CacheEvict(value = "users", allEntries = true)
	User save(User user);

}
