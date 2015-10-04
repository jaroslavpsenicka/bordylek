package org.bordylek.service.repository;

import org.bordylek.service.model.blog.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
