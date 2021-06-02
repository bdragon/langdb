CREATE TABLE iso_3166_1 (
  alpha2 text NOT NULL PRIMARY KEY,
  alpha3 text NOT NULL UNIQUE,
  name   text NOT NULL
);
