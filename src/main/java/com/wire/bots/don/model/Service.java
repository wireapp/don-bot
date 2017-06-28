package com.wire.bots.don.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 24/10/16
 * Time: 16:01
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Service {
    public PubKey[] public_keys;
    public String name;
    @JsonProperty("base_url")
    public String url;
    public String description;
    @JsonProperty("public_key")
    public String pubKey;
    public String[] tags;
    public String token;
    public String id;
    public String provider;
    public Boolean enabled;
    public String[] auth_tokens;

    @JsonProperty
    public List<Asset> assets;
}
