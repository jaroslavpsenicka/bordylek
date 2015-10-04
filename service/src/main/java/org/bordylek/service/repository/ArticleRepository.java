package org.bordylek.service.repository;

import org.bordylek.service.model.blog.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleRepository extends MongoRepository<Article, String> {

    Article findByTitle(String title);
    Page<Article> findByTitleLike(String title, Pageable pageable);

}
