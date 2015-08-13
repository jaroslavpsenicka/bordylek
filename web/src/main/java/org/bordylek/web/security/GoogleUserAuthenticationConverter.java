package org.bordylek.web.security;

import org.bordylek.service.event.EventQueue;
import org.bordylek.service.event.NewUserEvent;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Date;
import java.util.Map;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class GoogleUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventQueue eventQueue;

    private String registrar;
    private String defaultAuthorities;

    private static final String EMAIL = "email";
    private static final String USER_ID = "user_id";
    private static final String DISPLAY_NAME = "displayName";
    private static final String IMAGE = "image";

    private static Logger LOG = LoggerFactory.getLogger(GoogleUserAuthenticationConverter.class);

    @Required
    public void setRegistrar(String registrar) {
        this.registrar = registrar;
    }

    @Required
    public void setDefaultAuthorities(String defaultAuthorities) {
        this.defaultAuthorities = defaultAuthorities;
    }

    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USER_ID) && map.containsKey(EMAIL) && map.containsKey(DISPLAY_NAME)) {
            org.springframework.security.core.userdetails.User user = findOrCreateUser(map);
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
        }

        return null;
    }

    private org.springframework.security.core.userdetails.User findOrCreateUser(Map<String, ?> map) {
        String regId = registrar + "/" + map.get(USER_ID);
        User user = userRepository.findByRegId(regId);
        if (user == null) {
            user = new User();
            user.setRegId(regId);
            user.setCreateDate(new Date());
        }

        user.setName((String) map.get(DISPLAY_NAME));
        user.setEmail((String) map.get(EMAIL));

        Map image = (Map) map.get(IMAGE);
        if (image != null && image.containsKey("url") && !image.get("url").equals(user.getImageUrl())) {
            user.setImageUrl((String) image.get("url"));
        }

        boolean newUser = user.getId() == null;
        userRepository.save(user);
        if (newUser) {
            LOG.info("New user created: " + user.getName());
            eventQueue.send(new NewUserEvent(user));
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), "",
            createAuthorityList(defaultAuthorities));
    }

}
