package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 24/10/16
 * Time: 16:10
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthToken {
    public String auth_token;
    public String id;
}
