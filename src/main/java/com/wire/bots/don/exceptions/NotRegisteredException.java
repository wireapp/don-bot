package com.wire.bots.don.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 17:32
 */
public class NotRegisteredException extends Exception {
    public NotRegisteredException(String bot) {
        super("Not registered exception for botId: " + bot);
    }
}
