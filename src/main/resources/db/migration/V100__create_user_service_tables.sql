CREATE TABLE Developer (
    UserId VARCHAR PRIMARY KEY,
    Name VARCHAR,
    Email VARCHAR,
    WebSite VARCHAR,
    Description VARCHAR,
    Cookie VARCHAR,
    Provider VARCHAR
);

CREATE TABLE Service (
    Id SERIAL PRIMARY KEY,
    ServiceId VARCHAR,
    Name VARCHAR,
    Field VARCHAR,
    Url VARCHAR,
    Description VARCHAR,
    Profile VARCHAR
);