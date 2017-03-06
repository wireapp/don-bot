package com.wire.bots.don.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 17:40
 */
public class FailedAuthenticationException extends Exception {
    public FailedAuthenticationException(String msg) {
        super(msg);
    }
}
