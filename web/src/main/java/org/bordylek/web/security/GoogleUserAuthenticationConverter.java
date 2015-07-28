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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

public class GoogleUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventQueue eventQueue;

    private String defaultAuthorities;

    private static final String EMAIL = "email";
    private static final String USER_ID = "user_id";
    private static final String DISPLAY_NAME = "displayName";
    private static final String IMAGE = "image";

    private static Logger LOG = LoggerFactory.getLogger(GoogleUserAuthenticationConverter.class);

    @Required
    public void setDefaultAuthorities(String defaultAuthorities) {
        this.defaultAuthorities = defaultAuthorities;
    }

    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USER_ID) && map.containsKey(EMAIL) && map.containsKey(DISPLAY_NAME)) {
            User user = updateUser(findOrCreateUser(map), map);
            List<GrantedAuthority> auth = commaSeparatedStringToAuthorityList(defaultAuthorities);
            return new UsernamePasswordAuthenticationToken(user, "", auth);
        }

        return null;
    }

    private User findOrCreateUser(Map<String, ?> map) {
        String regId = "GOOGLE/" + map.get(USER_ID);
        User user = userRepository.findByRegId(regId);
        if (user == null) {
            user = new User();
            user.setRegId(regId);
            user.setCreateDate(new Date());
        }

        return user;
    }

    private User updateUser(User user, Map<String, ?> map) {
        boolean save = false;

        String name = (String) map.get(DISPLAY_NAME);
        String email = (String) map.get(EMAIL);
        if (!name.equals(user.getName()) || !email.equals(user.getEmail())) {
            user.setName(name);
            user.setEmail(email);
            save = true;
        }

        Map image = (Map) map.get(IMAGE);
        if (image != null && image.containsKey("url") && !image.get("url").equals(user.getImageUrl())) {
            user.setImageUrl((String) image.get("url"));
            save = true;
        }

        if (save) {
            boolean newUser = user.getId() == null;
            userRepository.save(user);
            if (newUser) {
                LOG.info("New user created: " + user.getName());
                eventQueue.send(new NewUserEvent(user));
            }
        }

        return user;
    }

}
