package org.bordylek.mon;

import org.bordylek.service.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = "/user/me", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public MeResponse findMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NotFoundException("user not known");
        }

        User user = (User) authentication.getPrincipal();
        return new MeResponse(user.getUsername(), user.getAuthorities());
    }

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

    @ExceptionHandler(IllegalAccessException.class)
    public void handleIllegalAccessException(IllegalAccessException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    public static class MeResponse {

        private String name;
        private Map<String, Integer> roles;

        public MeResponse() {
        }

        public MeResponse(String name, Collection<GrantedAuthority> roles) {
            this.name = name;
            this.roles = new HashMap<>();
            for (GrantedAuthority authority : roles) {
                this.roles.put(authority.getAuthority(), 1);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Integer> getRoles() {
            return roles;
        }

        public void setRoles(Map<String, Integer> roles) {
            this.roles = roles;
        }
    }

}
