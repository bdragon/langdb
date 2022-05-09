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
* ISO 15924: Codes for the representation of names of scripts
* ISO 3166-1: Country codes <sup>†</sup>
* UN M49: Standard area or country codes for statistical use <sup>†</sup>
* Unicode CLDR BCP 47 extensions U and T

<sup>†</sup> _These standards do not provide a download option, and including the data
as part of this project would be a copyright violation. The database schema
includes tables for the data, but gathering, processing, and inserting it
into the database is left as an exercise._

I created this project so that I could explore this data in one place;
your mileage may vary.

It is primarily written in Java and organized as a multi-module Maven
project. The workflow to rebuild the database is Docker-based, so you don't
need a Java environment unless you want to hack on it.

For a detailed overview of BCP 47 Language Tags,
see my blog post: https://bryandragon.com/blog/naming-languages

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

The database schema is defined in a series of SQL migration files under
[`load/migrations/`](load/migrations/). Below is a summary showing how the
various tables map to the JSON data and the original source data.

### ISO 639-2

**Source:** `data/raw/ISO-639-2_utf-8.txt`

**JSON:** `data/json/iso-639-2.json`

**Table:** `iso_639_2`

| Column     | JSON           | Source       | Description |
|------------|----------------|--------------|-------------|
| `b_id`     | `Part2B`       | Part2B       | ISO 639-2/B code |
| `t_id`     | `Part2T`       | Part2T       | ISO 639-2/T code |
| `part1`    | `Part1`        | Part1        | ISO 639-1 code |
| `name`     | `English_Name` | English Name | Language name |
|            | `French_Name`  | French Name  |             |
| `reserved` |                |              | True if code is reserved for local use |

Note: codes in the range `qaa..qtz` are reserved for local use.

### ISO 639-3

**Source:** `data/raw/iso-639-3.tab`

**JSON:** `data/json/iso-639-3.json`

**Table:** `iso_639_3`

| Column    | JSON            | Source          | Description |
|-----------|-----------------|-----------------|-------------|
| `id`      | `Id`            | `Id`            | ISO 639-3 code |
|           | `Part2B`        | `Part2B`        | ISO 639-2/B code |
| `part2t`  | `Part2T`        | `Part2T`        | ISO 639-2/T code |
| `part1`   | `Part1`         | `Part1`         | ISO 639-1 code |
| `scope`   | `Scope`         | `Scope`         | (I)ndividual, (M)acrolanguage, or (S)pecial |
| `type`    | `Language_Type` | `Language_Type` | (A)ncient, (C)onstructed, (E)xtinct, (H)istorical, (L)iving, or (S)pecial |
| `name`    | `Ref_Name`      | `Ref_Name`      | Language name |
| `comment` | `Comment`       | `Comment`       |             |

### ISO 639-3 Name Index

**Source:** `data/raw/iso-639-3_Name_Index.tab`

**JSON:** `data/json/iso-639-3-name-index.json`

**Table:** `iso_639_3_name`

| Column     | JSON            | Source          | Description |
|------------|-----------------|-----------------|-------------|
| `id`       | `Id`            | `Id`            | ISO 639-3 code |
| `print`    | `Print_Name`    | `Print_Name`    | Print name  |
| `inverted` | `Inverted_Name` | `Inverted_Name` | Language name root, if any, followed by language name |

### ISO 639-3 Macrolanguage Mappings

**Source:** `data/raw/iso-639-3-macrolanguages.tab`

**JSON:** `data/json/iso-639-3-macrolanguages.json`

**Table:** `iso_639_3_macrolanguage`

| Column | JSON   | Source     | Description |
|--------|--------|------------|-------------|
| `m_id` | `M_Id` | `M_Id`     | ISO 639-3 code of the macrolanguage |
| `i_id` | `I_Id` | `I_Id`     | ISO 639-3 code of the individual language |
|        |        | `I_Status` | Status of the individual language: (A)ctive, (R)etired |

### ISO 639-3 Retirements

**Source:** `data/raw/iso-639-3_Retirements.tab`

**JSON:** `data/json/iso-639-3-retirements.json`

**Table:** `iso_639_3_deprecation`

| Column         | JSON          | Source       | Description |
|----------------|---------------|--------------|-------------|
| `id`           | `Id`          | `Id`         | ISO 639-3 code |
|                | `Ref_Name`    | `Ref_Name`   | Language name |
| `reason`       | `Ret_Reason`  | `Ret_Reason` | (C)hange, (D)uplicate, (M)erge, (N)on-existent, (S)plit |
| `change_to`    | `Change_To`   | `Change_To`  | Change to this identifier in the case of a change, duplicate, or merge |
| `remedy`       | `Ret_Remedy`  | `Ret_Remedy` | How to resolve in the case of a split identifier |
| `effective_on` | `Effective`   | `Effective`  | Effective date |

### ISO 639-5

**Source:** `data/raw/iso639-5.tsv`

**JSON:** `data/json/iso-639-5.json`

**Table:** `iso_639_5`

| Column | JSON              | Source            | Description |
|--------|-------------------|-------------------|-------------|
|        | `URI`             | `URI`             |             |
| `id`   | `code`            | `code`            | ISO 639-5 code |
| `name` | `Label (English)` | `Label (English)` | Name of language family or group |
|        | `Label (French)`  | `Label (French)`  |             |

### ISO 15924

**Source:** `data/raw/iso15924.txt`

**JSON:** `data/json/iso-15924.json`

**Table:** `iso_15924`

| Column     | JSON             | Source          | Description |
|------------|------------------|-----------------|-------------|
| `alpha`    | `code`           | Code            | 4-letter code |
| `num`      | `number`         | N°              | 3-digit code |
| `name`     | `englishName`    | English Name    | Script name |
|            | `frenchName`     | Nom français    |             |
| `pva`      | `pva`            | PVA             | Property value attribute |
|            | `unicodeVersion` | Unicode Version |             |
|            | `date`           | Date            |             |
| `reserved` |                  |                 | True if code is reserved for private use |

Note: codes in the range `Qaaa..Qabx` are reserved for private use.

Note: while they are not assigned an explicit entry, the codes `True` and
`Root` are reserved in ISO 15924.

### ISO 3166-1

**Source:** `https://www.iso.org/obp/ui/`

**Table:** `iso_3166_1`

| Column   | JSON | Source               | Description |
|----------|------|----------------------|-------------|
| `name`   |      | `English short name` | Country name |
|          |      | `French short name`  |             |
| `alpha2` |      | `Alpha-2 code`       | 2-letter code |
| `alpha3` |      | `Alpha-3 code`       | 3-letter code |
| `num`    |      | `Numeric`            | 3-digit code |

### UN M49

**Source:** `https://unstats.un.org/unsd/methodology/m49/overview/`

**Table:** `un_m49`

| Column       | JSON | Source                                    | Description |
|--------------|------|-------------------------------------------|-------------|
| `id`         |      | `M49 Code`<br>`Intermediate Region Code`<br>`Sub-region Code`<br>`Region Code`<br>`Global Code` | UN M49 Code |
| `name`       |      | `Country or Area`<br>`Intermediate Region Name`<br>`Sub-region Name`<br>`Region Name`<br>`Global Name` | Name |
| `iso_alpha2` |      | `ISO-alpha2 Code`                         | ISO 3166-1 alpha2 code |
|              |      | `ISO-alpha3 Code`                         |             |
|              |      | `Least Developed Countries (LDC)`         |             |
|              |      | `Land Locked Developing Countries (LLDC)` |             |
|              |      | `Small Island Developing States (SIDS)`   |             |
|              |      | `Developed / Developing Countries`        |             |
| `type`       |      |                                           | `global`, `region`, `sub-region`,<br>`intermediate region`,<br>`country or area` |

### IANA Language Subtag Registry

**Source:** `data/raw/language-subtag-registry`

**JSON:** `data/json/language-subtag-registry.json`

**Table:** `subtag`

| Column            | JSON              | Source            | Description |
|-------------------|-------------------|-------------------|-------------|
| `type`            | `Type`            | `Type`            | `language`, `extlang`, `script`, `region`,<br>`variant`, `grandfathered`, `redundant` |
| `id`              | `Subtag`<br>`Tag` | `Subtag`<br>`Tag` |             |
| `description`     | `Description`     | `Description`     |             |
| `added_on`        | `Added`           | `Added`           | Date added  |
| `deprecated_on`   | `Deprecated`      | `Deprecated`      | Date deprecated |
| `preferred_value` | `Preferred-Value` | `Preferred-Value` |             |
| `suppress_script` | `Suppress-Script` | `Suppress-Script` |             |
| `macrolanguage`   | `Macrolanguage`   | `Macrolanguage`   |             |
| `scope`           | `Scope`           | `Scope`           | `collection`, `individual`, `macrolanguage`,<br>`private-use`, `special` |
| `comments`        | `Comments`        | `Comments`        |             |

**Table:** `subtag_prefix`

| Column        | JSON     | Source   | Description |
|---------------|----------|----------|-------------|
| `subtag_type` |          |          |             |
| `subtag_id`   |          |          |             |
| `prefix`      | `Prefix` | `Prefix` |             |

### IANA Language Tag Extensions Registry

**Source:** `data/raw/language-tag-extensions-registry`

**JSON:** `data/json/language-tag-extensions-registry.json`

**Table:** `subtag_ext`

| Column        | JSON            | Source          | Description |
|---------------|-----------------|-----------------|-------------|
| `id`          | `Identifier`    | `Identifier`    | Extension letter |
| `description` | `Description`   | `Description`   |             |
| `comments`    | `Comments`      | `Comments`      |             |
| `added_on`    | `Added`         | `Added`         | Date added  |
|               | `RFC`           | `RFC`           |             |
|               | `Authority`     | `Authority`     |             |
|               | `Contact_Email` | `Contact_Email` |             |
|               | `Mailing_List`  | `Mailing_List`  |             |
|               | `URL`           | `URL`           |             |

### CLDR BCP 47 Extensions

**Source:** `data/raw/cldr/bcp47/*`

**JSON:** `data/json/cldr-bcp47-extensions.json`

**Table:** `subtag_ext_key`

| Column        | JSON                    | Source                 | Description |
|---------------|-------------------------|------------------------|-------------|
| `ext_id`      | `$.keys[*].extension`   | `//key/[@extension]`   | Extension letter |
| `id`          | `$.keys[*].name`        | `//key/[@name]`        | Key name    |
| `description` | `$.keys[*].description` | `//key/[@description]` |             | 
| `deprecated`  | `$.keys[*].deprecated`  | `//key[@deprecated]`   |             |
| `preferred`   | `$.keys[*].preferred`   | `//key[@preferred]`    |             |
| `alias`       | `$.keys[*].alias`       | `//key[@alias]`        |             |
| `value_type`  | `$.keys[*].valueType`   | `//key[@valueType]`    | Single, multiple, incremental, any |
|               | `$.keys[*].since`       | `//key[@since]`        | Since Unicode version |

**Table:** `subtag_ext_key_type`

| Column        | JSON                             | Source                     | Description |
|---------------|----------------------------------|----------------------------|-------------|
| `ext_id`      | `$.keys[*].extension`            | `//key[@extension]`        | Extension letter |
| `key_id`      | `$.keys[*].name`                 | `//key[@name]`             | Key name    |
| `id`          | `$.keys[*].types[*].name`        | `//key/type[@name]`        | Key type name |
| `description` | `$.keys[*].types[*].description` | `//key/type[@description]` |             |
| `deprecated`  | `$.keys[*].types[*].deprecated`  | `//key/type[@deprecated]`  |             |
| `preferred`   | `$.keys[*].types[*].preferred`   | `//key/type[@preferred]`   |             |
| `alias`       | `$.keys[*].types[*].alias`       | `//key/type[@alias]`       |             |
|               | `$.keys[*].types[*].since`       | `//key/type[@since]`       | Since Unicode version |

**Table:** `subtag_ext_key_attr`

| Column        | JSON                          | Source                      | Description |
|---------------|-------------------------------|-----------------------------|-------------|
| `id`          | `$.attributes[*].name`        | `//attribute[@name]`        | Key type name |
| `description` | `$.attributes[*].description` | `//attribute[@description]` |             |
| `deprecated`  | `$.attributes[*].deprecated`  | `//attribute[@deprecated]`  |             |
| `preferred`   | `$.attributes[*].preferred`   | `//attribute[@preferred]`   |             |
|               | `$.attributes[*].since`       | `//attribute[@since]`       | Since Unicode version |

## Building the Database

The database is built in a series of steps, each corresponding to a Make task:
download, transform, load, build.

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

### 2. Transform

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

### 3. Load

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

### 4. Build

<table>
<tbody>
<tr><td>Dockerfile</td><td><code>build/Dockerfile</code></td></tr>
<tr><td>Make task</td><td><code>make build</code></td></tr>
</tbody>
</table>

Builds and tags the final Docker image, which is based on
[postgres:13-alpine](https://hub.docker.com/_/postgres).

The Docker image contains the dump of the fully-loaded database, which is used
to seed the database when the container starts up. Of course, you can also grab
the dump directly from `data/sql/` and do something else with it.

## Development

Requires JDK 17. Java source code is formatted with
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

This project makes use of open source, publicly available data provided by
[iana.org](https://iana.org/),
[iso.org](https://iso.org/),
[iso639-3.sil.org](https://iso639-3.sil.org/),
[loc.gov](https://loc.gov/),
and [unicode.org](https://unicode.org/).
When using data downloaded from one of these sources, please review the
terms and conditions to ensure that your use is appropriate.
