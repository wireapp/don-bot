package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 26/10/16
 * Time: 13:13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateService {
    @JsonProperty
    public String password;

    // connection
    @JsonProperty("base_url")
    public String url;
    public Boolean enabled;
    @JsonProperty("auth_tokens")
    public String[] tokens;
    @JsonProperty("public_keys")
    public String[] pubKeys;

    // service
    @JsonProperty
    public String description;

    @JsonProperty
    public String summary;

    @JsonProperty
    public List<Asset> assets;

    @JsonProperty
    public String name;
}
