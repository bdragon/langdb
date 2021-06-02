CREATE TABLE un_m49_region (
  id   text NOT NULL PRIMARY KEY,
  name text NOT NULL
);

-- ---------------------------------------------------------------------------

CREATE TABLE un_m49_country_area (
  id     text NOT NULL PRIMARY KEY,
  region text NOT NULL REFERENCES un_m49_region (id),
  alpha2 text     NULL REFERENCES iso_3166_1 (alpha2),
  name   text NOT NULL
);
