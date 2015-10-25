package org.bordylek.service.repository;

import org.bordylek.service.model.process.Voting;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotingRepository extends MongoRepository<Voting, String> {

    @Cacheable("votes")
    List<Voting> findByCommunity(String communityId);
}
