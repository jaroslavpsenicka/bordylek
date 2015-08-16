package org.bordylek.web.security;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class AutoConnectionSignUp implements ConnectionSignUp {

    @Override
    public String execute(Connection<?> connection) {
        return connection.getKey().toString();
    }
}
