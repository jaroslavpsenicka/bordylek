package org.bordylek.web;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.event.EventQueue;
import org.bordylek.service.event.NewUserEvent;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class UserController {

	@Autowired
	private UserRepository repository;

	@Autowired
	private EventQueue eventQueue;

	@Value("${user.pageSize:25}")
	private int pageSize;
	
	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
	
	public void setEventQueue(EventQueue queue) {
		this.eventQueue = queue;
	}
	
	@RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public User create(@RequestBody User user) {
		user.setCreateDate(new Date());
		user = this.repository.save(user);
		LOG.info("New user created: "+user.getName());
		eventQueue.send(new NewUserEvent(user));
		return user;
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasPermission(#user, 'OWNER') or hasRole('ADMIN')")
	public User update(@PathVariable("id") String id, @RequestBody User user) {
		user.setId(id);
		return (User) this.repository.save(user);
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable("id") String id) {
		User user = (User) this.repository.findOne(id);
		if (user == null) throw new NotFoundException(id);
		user.setDeleted(true);
		this.repository.save(user);
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public User findOne(@PathVariable("id") String id) {
		User user = this.repository.findOne(id);
		if (user == null) throw new NotFoundException(id);
		return user;
	}
	
	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	public List<User> findAll(@RequestParam(value = "page", defaultValue = "0") int pageNumber) {
		Pageable request = new PageRequest(pageNumber, pageSize);
        return repository.findAll(request).getContent();
	}
	
	@ExceptionHandler(ValidationException.class)
	public void handleConstraintViolationException(ValidationException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

}
