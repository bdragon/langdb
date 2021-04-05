CREATE TABLE un_m49_type (
  type text NOT NULL PRIMARY KEY
);

INSERT INTO un_m49_type
  (type)
VALUES
  ('global'),
  ('region'),
  ('sub-region'),
  ('intermediate region'),
  ('country or area');

-- ---------------------------------------------------------------------------

CREATE TABLE un_m49 (
  id         text    NOT NULL PRIMARY KEY,
  name       text    NOT NULL,
  type       text    NOT NULL REFERENCES un_m49_type (type),
  parent_id  text        NULL REFERENCES un_m49 (id),
  iso_alpha2 text        NULL REFERENCES iso_3166_1 (alpha2)
);
