CREATE TABLE subtag_type (
  type text NOT NULL PRIMARY KEY
);

INSERT INTO subtag_type
  (type)
VALUES
  ('extlang'),
  ('grandfathered'),
  ('language'),
  ('redundant'),
  ('region'),
  ('script'),
  ('variant');

-- ---------------------------------------------------------------------------

CREATE TABLE subtag_scope (
  scope text NOT NULL PRIMARY KEY
);

INSERT INTO subtag_scope
  (scope)
VALUES
  ('collection'),
  ('individual'),
  ('macrolanguage'),
  ('private-use'),
  ('special');

-- ---------------------------------------------------------------------------

CREATE TABLE subtag (
  type            text    NOT NULL REFERENCES subtag_type (type),
  id              text    NOT NULL,
  description     text        NULL,
  added_on        date    NOT NULL,
  deprecated_on   date        NULL,
  preferred_value text        NULL,
  suppress_script text        NULL,
  macrolanguage   text        NULL,
  scope           text        NULL REFERENCES subtag_scope (scope),
  comments        text        NULL,
  PRIMARY KEY (type, id)
);

-- ---------------------------------------------------------------------------

CREATE TABLE subtag_prefix (
  subtag_type text NOT NULL,
  subtag_id   text NOT NULL,
  prefix      text NOT NULL,
  PRIMARY KEY (subtag_type, subtag_id, prefix),
  FOREIGN KEY (subtag_type, subtag_id) REFERENCES subtag (type, id)
);
