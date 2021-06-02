# langdb

[![CI](https://github.com/bdragon/langdb/actions/workflows/ci.yml/badge.svg)](https://github.com/bdragon/langdb/actions/workflows/ci.yml)

## Introduction

This project gathers standards data related to
[BCP 47 language tags](https://tools.ietf.org/html/bcp47),
transforms it to JSON,
and inserts it into a SQL database. It covers:

* IANA language subtags and language tag extensions
* ISO 639: Codes for the representation of names of languages
  * ISO 639-1: Alpha-2 code
  * ISO 639-2: Alpha-3 code
  * ISO 639-3: Alpha-3 code for comprehensive coverage of languages
  * ISO 639-5: Alpha-3 code for language families and groups
  * ISO 639 macrolanguage mappings
* ISO 3166-1: Country codes
* ISO 15924: Codes for the representation of names of scripts
* UN M49: Standard area or country codes for statistical use
* Unicode CLDR BCP 47 extensions U and T

I created this project so that I could explore this data in one place;
your mileage may vary.

The project is primarily written in Java and organized as a multi-module Maven
project. Don't want to set up a Java environment? A Docker-based workflow is
included so that you don't have to.

## Data

The `data/` directory is used for reading and writing data as the database is
built, and the latest version of the data is committed there.

* [`data/raw/`](data/raw/) contains the original source data
* [`data/json/`](data/json/) contains the processed data in JSON format
* [`data/sql/`](data/sql/) contains a SQL dump of the populated database

## Running Queries

Here is an example Docker workflow that starts the database in one container
and connects to it with a psql shell in a separate postgres container.

```
docker network create langdb

# Start the database server in the background.
docker run -dit --network=langdb -h langdb bryandragon/langdb

# Wait a minute and start a postgresql client session in the foreground.
docker run -it --network=langdb -e PGPASSWORD=langdb \
  postgres:13-alpine psql -h langdb -U langdb
psql (13.2)
Type "help" for help.

langdb=# select k.ext_id, k.id, count(*) as qty from subtag_ext_key as k
join subtag_ext_key_type as t on k.ext_id = t.ext_id and k.id = t.key_id
group by k.ext_id, k.id order by qty desc, k.ext_id, k.id limit 5;
 ext_id | id | qty
--------+----+-----
 u      | tz | 466
 u      | cu | 303
 u      | nu |  88
 t      | k0 |  29
 t      | d0 |  22
(5 rows)
```

## Schema

The database schema is defined as a series of SQL migration files under
[`load/migrations/`](load/migrations/). Below is a summary of how the various
tables map to the original source data.

### ISO 639-5

**Source:** `data/raw/iso639-5.tsv`

**JSON:** `data/json/iso-639-5.json`

**Table:** `iso_639_5`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| | `URI` | `URI` |
| `id` | `code` | `code` | ISO 639-5 code |
| `name` | `Label (English)` | `Label (English)` | Name of language family or group |
| | `Label (French)` | `Label (French)` |

### ISO 639-2

**Source:** `data/raw/ISO-639-2_utf-8.txt`

**JSON:** `data/json/iso-639-2.json`

**Table:** `iso_639_2`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `t_id` | `Part2T` | | ISO 639-2/T code |
| `b_id` | `Part2B` | | ISO 639-2/B code |
| `name` | `English_Name` |  | |
| | `French_Name` | | |

### ISO 639-2 Changes

**JSON:** `data/json/iso-639-2-changes.json`

**Table:** `iso_639_2_change`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `part1` | `ISO 639-1 Code` | | ISO 639-1 code |
| `part2t` | `ISO 639-2 Code` | | ISO 639-2/T code|
| | `English name of Language` | | |
| | `French name of Language` | | |
| | `Date Added or Changed` | | |
| `type` | `Category of Change` | | Added, deprecated, code changed, name changed, variant name(s) added |
| | `Notes` | | |

### ISO 639-3

**Source:** `data/raw/iso-639-3.tab`

**JSON:** `data/json/iso-639-3.json`

**Table:** `iso_639_3`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `id` | `Id` | `Id` | ISO 639-3 code |
| `part1` | `Part1` | `Part1` | ISO 639-1 code |
| `part2t` | `Part2T` | `Part2T` | ISO 639-2/T code |
| `part2b` | `Part2B` | `Part2B` | ISO 639-2/B code |
| `scope` | `Scope` | `Scope` | Individual, macrolanguage, or special |
| `type` | `Language_Type` | `Language_Type` | Ancient, constructed, extinct, historical, living, or special |
| `name` | `Ref_Name` | `Ref_Name` | Language name |
| `comment` | `Comment` | `Comment` | |

### ISO 639-3 Name Index

**Source:** `data/raw/iso-639-3_Name_Index.tab`

**JSON:** `data/json/iso-639-3-name-index.json`

**Table:** `iso_639_3_name`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `id` | `Id` | `Id` | ISO 639-3 code |
| `print` | `Print_Name` | `Print_Name` | Print name |
| `inverted` | `Inverted_Name` | `Inverted_Name` | Language name root, if any, followed by language name |

### ISO 639-3 Macrolanguage Mappings

**Source:** `data/raw/iso-639-3-macrolanguages.tab`

**JSON:** `data/json/iso-639-3-macrolanguages.json`

Table: `iso_639_3_macrolanguage`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `m_id` | `M_Id` | `M_Id` | ISO 639-3 code of the macrolanguage |
| `i_id` | `I_Id` | `I_Id` | ISO 639-3 code of the individual language |

### ISO 639-3 Retirements

**Source:** `data/raw/iso-639-3_Retirements.tab`

**JSON:** `data/json/iso-639-3-retirements.json`

**Table:** `iso_639_3_deprecation`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `id` | `Id` | `Id` | ISO 639-3 code |
| | `Ref_Name` | `Ref_Name` | Language name |
| `reason` | `Ret_Reason` | `Ret_Reason` | Change, duplicate, merge, nonexistent, or split |
| `change_to` | `Change_To` | `Change_To` | Code that should be used instead |
| `remedy` | `Ret_Remedy` | `Ret_Remedy` | How to resolve in the case of a split |
| `effective` | `Effective` | `Effective` | Date on which the deprecation took effect |

### ISO 15924

**Source:** `data/raw/iso15924.txt`

**JSON:** `data/json/iso-15924.json`

**Table:** `iso_15924`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `alpha` | `code` | Code | 4-letter code |
| `num` | `number` | N° | 3-digit code  |
| `pva` | `pva` | PVA | Property Value Attribute |
| `name` | `englishName` | English Name | Script name |
| | | Nom français | |
| | | Unicode Version | |
| | | Date | |

### ISO 3166-1

**JSON:** `data/json/iso-3166-1.json`

**Table:** `iso_3166_1`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `alpha2` | `Alpha-2 code` | `Alpha-2 code` | 2-letter code |
| `alpha3` | `Alpha-3 code` | `Alpha-3 code` | 3-letter code |
| `name` | `English short name` | `English short name` | Country name |
| | `French short name` | `French short name` | |
| `num` | `Numeric` | `Numeric` | 3-digit code |

### UN M49

**JSON:** `data/json/un-m49.json`

**Table:** `un_m49_region`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| | `Global Code` | `Global Code` | |
| | `Global Name` | `Global Name` | |
| `id` | `Region Code` | `Region Code` | Region code |
| `name` | `Region Name` | `Region Name` | Region name |
| | `Sub-region Code` | `Sub-region Code` | |
| | `Sub-region Name` | `Sub-region Name` | |
| | `Intermediate Region Code` | `Intermediate Region Code` | |
| | `Intermediate Region Name` | `Intermediate Region Name` | |
| | `Country or Area` | `Country or Area` | |
| | `M49 Code` | `M49 Code` | |
| | `ISO-alpha2 Code` | `ISO-alpha2 Code` | |
| | `ISO-alpha3 Code` | `ISO-alpha3 Code` | |
| | `Least Developed Countries (LDC)` | `Least Developed Countries (LDC)` | |
| | `Land Locked Developing Countries (LLDC)` | `Land Locked Developing Countries (LLDC)` | |
| | `Small Island Developing States (SIDS)` | `Small Island Developing States (SIDS)` | |
| | `Developed / Developing Countries` | `Developed / Developing Countries` | |

**Table:** `un_m49_country_area`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| | `Global Code` | `Global Code` | |
| | `Global Name` | `Global Name` | |
| `region` | `Region Code` | `Region Code` | |
| | `Region Name` | `Region Name` | |
| | `Sub-region Code` | `Sub-region Code` | |
| | `Sub-region Name` | `Sub-region Name` | |
| | `Intermediate Region Code` | `Intermediate Region Code` | |
| | `Intermediate Region Name` | `Intermediate Region Name` | |
| `name` | `Country or Area` | `Country or Area` | Name of country or area |
| `id` | `M49 Code` | `M49 Code` | UN M49 code |
| `alpha2` | `ISO-alpha2 Code` | `ISO-alpha2 Code` | ISO 3166-1 code |
| | `ISO-alpha3 Code` | `ISO-alpha3 Code` | |
| | `Least Developed Countries (LDC)` | `Least Developed Countries (LDC)` | |
| | `Land Locked Developing Countries (LLDC)` | `Land Locked Developing Countries (LLDC)` | |
| | `Small Island Developing States (SIDS)` | `Small Island Developing States (SIDS)` | |
| | `Developed / Developing Countries` | `Developed / Developing Countries` | |

### IANA Language Subtag Registry

**Source:** `data/raw/language-subtag-registry`

**JSON:** `data/json/language-subtag-registry.json`

**Table:** `subtag`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `type` | `Type` | `Type` | Language, extlang, script, region,<br>variant, grandfathered, redundant |
| `id` | `Tag` / `Subtag` | `Tag` / `Subtag` | Subtag |
| `description` | `Description` | `Description` | |
| `added` | `Added` | `Added` | Date added |
| `deprecated` | `Deprecated` | `Deprecated` | Date deprecated |
| `preferred_value` | `Preferred-Value` | `Preferred-Value` | |
| `suppress_script` | `Suppress-Script` | `Suppress-Script` | |
| `macrolanguage` | `Macrolanguage` | `Macrolanguage` | |
| `scope` | `Scope` | `Scope` | Collection, individual, macrolanguage,<br>private-use, special |
| `comments` | `Comments` | `Comments` | |

**Table:** `subtag_prefix`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `subtag_type` | | | |
| `subtag_id` | | | |
| `prefix` | `Prefix` | `Prefix` | |

### IANA Language Tag Extensions Registry

**Source:** `data/raw/language-subtag-registry`

**JSON:** `data/json/language-subtag-registry.json`

**Table:** `subtag_ext`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `id` | `Identifier` | `Identifier` | |
| `description` | `Description` | `Description` | |
| `comments` | `Comments` | `Comments` | |
| `added` | `Added` | `Added` | Date added |
| | `RFC` | `RFC` | |
| | `Authority` | `Authority` | |
| | `Contact_Email` | `Contact_Email` | |
| | `Mailing_List` | `Mailing_List` | |
| | `URL` | `URL` | |

### CLDR BCP 47 Extensions

**Source:** `data/raw/cldr/bcp47/*`

**JSON:** `data/json/cldr-bcp47-extensions.json`

**Table:** `subtag_ext_key`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `ext_id` | `extension` | `//key/[@extension]` | |
| `id` | `name` | `//key/[@name]` | |
| `description` | `description` | `//key/[@description]` | | 
| `deprecated` | `deprecated` | `//key[@deprecated]` | |
| `preferred` | `preferred` | `//key[@preferred]` | |
| `alias` | `alias` | `//key/[@alias]` | |
| `value_type` | `valueType` | `//key[@valueType]` | Single, multiple, incremental, any |
| `since` | `since` | `//key[@since]` | |

**Table:** `subtag_ext_key_type`

| Column | JSON | Source | Description |
|--------|------|--------|-------------|
| `ext_id` | `extension` | `//key[@extension]` | |
| `key_id` | `name` | `//key[@name]` | |
| `id` | `types[*].name` | `//key/type[@name]` | |
| `description` | `types[*].description` | `//key/type[@description]` | |
| `deprecated` | `types[*].deprecated` | `//key/type/[@deprecated]` | |
| `preferred` | `types[*].preferred` | `//key/type[@preferred]` | |
| `alias` | `types[*].alias` | `//key/type[@alias]` | |
| `since` | `types[*].since` | `//key/type[@since]` | Since Unicode version |

## Building the Database

The database is built in a series of steps, each corresponding to a Make task:
download, extract, transform, load, build.

To gather the latest source data and fully rebuild the database, run the
default Make task:

```
make clean
make
```

Or you can run the steps individually.

### 1. Download

<table>
<tbody>
<tr><td>Dockerfile</td><td><code>build/Dockerfile-download</code></td></tr>
<tr><td>Docker entrypoint</td><td><code>build/scripts/download.sh</code></td></tr>
<tr><td>Make task</td><td><code>make download</code></td></tr>
</tbody>
</table>

Downloads original sources to `data/raw/`.

### 2. Extract

<table>
<tbody>
<tr><td>Dockerfile</td><td><code>build/Dockerfile-extract</code></td></tr>
<tr><td>Docker entrypoint</td><td><code>build/scripts/extract.sh</code></td></tr>
<tr><td>Maven module</td><td><code>langdb-extract</code></td></tr>
<tr><td>Source code</td><td><code>extract/</code></td></tr>
<tr><td>Make task</td><td><code>make extract</code></td></tr>
</tbody>
</table>

Gathers any additional sources that do not have a readily available download
option, namely:

* ISO 639-2 code changes
* ISO 3166-1
* UN M49

The extracted sources are written as JSON files to `data/json/`.

### 3. Transform

<table>
<tbody>
<tr><td>Dockerfile</td><td><code>build/Dockerfile-transform</code></td></tr>
<tr><td>Docker entrypoint</td><td><code>build/scripts/transform.sh</code></td></tr>
<tr><td>Maven module</td><td><code>langdb-transform</code></td></tr>
<tr><td>Source code</td><td><code>transform/</code></td></tr>
<tr><td>Make task</td><td><code>make transform</code></td></tr>
</tbody>
</table>

Converts all original sources into a consistent intermediate format (JSON)
to simplify later processing. Many of the original sources
are formatted as CSV or TSV data, but some are missing column headers.
Others use less readily consumable formats, such as
[record-jar](https://tools.ietf.org/html/draft-phillips-record-jar-01).

Each source is converted to JSON and written to a file under `data/json/`.
Database aside, having up-to-date standards data in JSON format may be useful
in its own right.

### 4. Load

<table>
<tbody>
<tr><td>Dockerfile</td><td><code>build/Dockerfile-load</code></td></tr>
<tr><td>Docker entrypoint</td><td><code>build/scripts/load.sh</code></td></tr>
<tr><td>Maven module</td><td><code>langdb-load</code></td></tr>
<tr><td>Source code</td><td><code>load/</code></td></tr>
<tr><td>SQL schema</td><td><code>load/migrations/</code></td></tr>
<tr><td>Make task</td><td><code>make load</code></td></tr>
</tbody>
</table>

Loads the data from the JSON files in `data/json/` and inserts it into the
database.

Once all data has been inserted, the database is dumped to `data/sql/`.

### 5. Build

<table>
<tbody>
<tr><td>Dockerfile</td><td><code>build/Dockerfile</code></td></tr>
<tr><td>Make task</td><td><code>make build</code></td></tr>
</tbody>
</table>

Builds and tags the final
[Docker image](https://hub.docker.com/repository/docker/bryandragon/langdb),
which is based on
[postgres:13-alpine](https://hub.docker.com/_/postgres).

The Docker image contains the dump of the fully-loaded database and it is
seeded when the container starts up. Of course, you can always grab
the dump directly from `data/sql/` and do something else with it.

## Development

Java source code is formatted with
[google-java-format](https://github.com/google/google-java-format)
and checked with [error\_prone](https://errorprone.info/)
and [Checkstyle](https://maven.apache.org/plugins/maven-checkstyle-plugin/index.html).

Common development tasks are handled with Maven.

Install and validate:

```
mvn install validate
```

Run tests:

```
mvn test
```

Run tests for a specific module (for example, `transform/`):

```
mvn -pl ./transform -am test
```

Run Checkstyle:

```
mvn checkstyle:check
```

## License and Copyright

This project is free and open source. See [LICENSE](LICENSE).

This project uses open source, publicly available data. Any and all data
downloaded from
[iana.org](https://iana.org/),
[iso.org](https://iso.org/),
[loc.gov](https://loc.gov/),
[iso639-3.sil.org](https://iso639-3.sil.org/),
[un.org](https://un.org/),
or [unicode.org](https://unicode.org/)
are bound by the copyright and terms and conditions of each.
