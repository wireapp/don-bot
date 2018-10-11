package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider {
    public String name;
    public String email;
    public String url;
    public String description;
    public String id;
    public String password;
}
