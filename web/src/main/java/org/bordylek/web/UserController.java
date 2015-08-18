package org.bordylek.web;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.event.EventQueue;
import org.bordylek.service.model.Location;
import org.bordylek.service.model.User;
import org.bordylek.service.model.UserStatus;
import org.bordylek.service.repository.UserRepository;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
public class UserController {

	@Autowired
	private UserRepository repository;

	@Autowired
	private EventQueue eventQueue;

	@Value("${user.pageSize:25}")
	private int pageSize;
	
	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = "/user/me", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public User findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedUserException("user not known");
        }

        String principal = (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
            ? ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername()
            : authentication.getPrincipal().toString();
        return this.repository.findByRegId(principal);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public User findOne(@PathVariable("id") String id) {
        User user = this.repository.findOne(id);
        if (user == null) throw new NotFoundException(id);
        return user;
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public User update(@PathVariable("id") String id, @RequestBody @Valid UserUpdateReq req) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedUserException("user not known");
        }
        String principal = (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
            ? ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername()
            : authentication.getPrincipal().toString();
        User updatingUser = this.repository.findByRegId(principal);
        if (updatingUser == null || !updatingUser.getId().equals(id)) throw new IllegalAccessException();

        User dbUser = this.repository.findOne(id);
        if (dbUser == null) throw new NotFoundException(id);
        dbUser.setName(req.getName());
        dbUser.setLocation(req.getLocation());
        dbUser.setStatus(UserStatus.VERIFIED);
        return this.repository.save(dbUser);
    }

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

    @ExceptionHandler(IllegalAccessException.class)
    public void handleIllegalAccessException(IllegalAccessException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public void unauthorizedUserException(UnauthorizedUserException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    public static class UserUpdateReq {

        @NotEmpty(message = "name may not be null")
        @Length(min = 3, max = 255, message = "name should be 3-255 characters long")
        private String name;

        @NotNull(message = "location may not be null")
        private Location location;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

}
