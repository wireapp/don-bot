package com.wire.bots.don.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 16:04
 */
public class AlreadyRegisteredException extends Exception {
    public AlreadyRegisteredException(String botId) {
        super("Already registered. BotId: " + botId);
    }
}
