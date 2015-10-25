package org.bordylek.service;

public class ExpiredException extends Exception {

    public ExpiredException(String id) {
        super(id);
    }
}
