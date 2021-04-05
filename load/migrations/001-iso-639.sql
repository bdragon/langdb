CREATE TABLE iso_639_5 (
  id   text NOT NULL PRIMARY KEY,
  name text NOT NULL
);

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_2 (
  t_id     text NOT NULL PRIMARY KEY,
  b_id     text NOT NULL UNIQUE,
  part1    text     NULL UNIQUE,
  name     text NOT NULL,
  reserved boolean NOT NULL DEFAULT false
);

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3_scope (
  scope text NOT NULL PRIMARY KEY
);

INSERT INTO iso_639_3_scope
  (scope)
VALUES
  ('individual'),
  ('macrolanguage'),
  ('special');

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3_type (
  type text NOT NULL PRIMARY KEY
);

INSERT INTO iso_639_3_type
  (type)
VALUES
  ('ancient'),
  ('constructed'),
  ('extinct'),
  ('historical'),
  ('living'),
  ('special');

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3 (
  id      text NOT NULL PRIMARY KEY,
  part2t  text     NULL REFERENCES iso_639_2 (t_id),
  part1   text     NULL UNIQUE,
  scope   text NOT NULL REFERENCES iso_639_3_scope (scope),
  type    text NOT NULL REFERENCES iso_639_3_type (type),
  name    text NOT NULL,
  comment text
);

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3_name (
  id       text NOT NULL REFERENCES iso_639_3 (id),
  print    text NOT NULL,
  inverted text NOT NULL,
  PRIMARY KEY (id, print)
);

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3_macrolanguage (
  m_id text NOT NULL,
  i_id text NOT NULL,
  PRIMARY KEY (m_id, i_id)
);

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3_deprecation_reason (
  reason text NOT NULL PRIMARY KEY
);

INSERT INTO iso_639_3_deprecation_reason
  (reason)
VALUES
  ('change'),
  ('duplicate'),
  ('merge'),
  ('nonexistent'),
  ('split');

-- ---------------------------------------------------------------------------

CREATE TABLE iso_639_3_deprecation (
  id           text NOT NULL,
  reason       text     NULL REFERENCES iso_639_3_deprecation_reason (reason),
  change_to    text     NULL,
  remedy       text     NULL,
  effective_on date NOT NULL,
  PRIMARY KEY (id, effective_on)
);
