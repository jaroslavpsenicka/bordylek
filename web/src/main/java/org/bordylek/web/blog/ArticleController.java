package org.bordylek.web.blog;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.model.blog.Article;
import org.bordylek.service.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/blog")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @ResponseBody
    @RequestMapping(value = "/article", method = RequestMethod.GET, produces = "application/json")
    public List<Article> findAllArticles() {
        return articleRepository.findAll();
    }

    @ResponseBody
    @RequestMapping(value = "/article/{id}", method = RequestMethod.GET, produces = "application/json")
    public Article findById(@PathVariable("id") String id) {
        Article article = articleRepository.findOne(id);
        if (article == null) throw new NotFoundException(id);
        return article;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/article", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid Article article) {
        article.setId(null);
        article.setCreateDate(new Date());
        article.setValid(false);
        articleRepository.save(article);
    }

    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/article", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody @Valid Article article) {
        if (article.getId() == null) throw new IllegalArgumentException();
        articleRepository.save(article);
    }

    @RequestMapping(value = "/article/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") String id) {
        Article article = articleRepository.findOne(id);
        if (article == null) throw new NotFoundException(id);
        articleRepository.delete(article);
    }

    @ExceptionHandler(ValidationException.class)
    public void handleConstraintViolationException(ValidationException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(NotFoundException.class)
    public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(NotFoundException.class)
    public void handleIllegalArgumentException(IllegalArgumentException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }

}
