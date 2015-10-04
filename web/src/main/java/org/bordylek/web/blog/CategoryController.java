package org.bordylek.web.blog;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.model.blog.Category;
import org.bordylek.service.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;

@Controller
@RequestMapping(value = "/blog")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @ResponseBody
    @RequestMapping(value = "/category", method = RequestMethod.GET, produces = "application/json")
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @ResponseBody
    @RequestMapping(value = "/category/{id}", method = RequestMethod.GET, produces = "application/json")
    public Category findById(@PathVariable final String id) {
        Category category = categoryRepository.findOne(id);
        if (category == null) throw new NotFoundException(id);
        return category;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/category", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid Category category) {
        category.setId(null);
        categoryRepository.save(category);
    }

    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/category", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody @Valid Category category) {
        if (category.getId() == null) throw new IllegalArgumentException();
        categoryRepository.save(category);
    }

    @RequestMapping(value = "/category/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") String id) {
        Category category = categoryRepository.findOne(id);
        if (category == null) throw new NotFoundException(id);
        categoryRepository.delete(category);
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
