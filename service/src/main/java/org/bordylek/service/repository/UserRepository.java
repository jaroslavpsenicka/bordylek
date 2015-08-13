package org.bordylek.service.repository;

import org.bordylek.service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	User findByRegId(String regId);
	User findByEmail(String email);
}
