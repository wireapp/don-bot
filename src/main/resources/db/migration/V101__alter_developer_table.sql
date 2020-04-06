DROP TABLE Developer;

CREATE TABLE Developer (
    UserId UUID PRIMARY KEY,
    Name VARCHAR,
    Email VARCHAR,
    WebSite VARCHAR,
    Description VARCHAR,
    Cookie VARCHAR,
    Provider VARCHAR
);