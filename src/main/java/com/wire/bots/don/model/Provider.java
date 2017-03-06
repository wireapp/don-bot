package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 24/10/16
 * Time: 13:05
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider {
    public String name;
    public String email;
    public String url;
    public String description;
    public String id;
}
