# langdb

## Introduction

This project builds a database of standards data related to [BCP 47 language
tags](https://tools.ietf.org/html/bcp47), including:

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

I created it because I wanted to explore this data in one place; your mileage
may vary.

The project is primarily written in Java and organized as a multi-module Maven
project. Don't want to set up a Java environment? A Docker-based workflow is
included so that you don't have to.

## Building the Database

The database is built in a series of steps: download, extract, transform, load.
The project is organized around these steps, and the Makefile provides tasks
with the same names.

The `data/` directory is used for reading and writing data throughout the
process, and the latest version of the data is committed there.

To gather the latest source data and fully rebuild the database, run the
default Make task:

```
make clean
make
```

Or you can run the steps individually.

### Download

```
make download
```

The [download.sh](build/scripts/download.sh) script downloads original sources
to `data/raw/`.

### Extract

```
make extract
```

The `extract/` module gathers any additional sources that do not have a readily
available download option, namely:

* ISO 639-2 code changes
* ISO 3166-1
* UN M49

The extracted sources are written as JSON files to `data/json/`.

### Transform

```
make transform
```

The `transform/` module converts all original sources into a consistent
intermediate format (JSON) to simplify later processing. Many original sources
are formatted as CSV or TSV data but some are missing a header row;
others use less readily consumable formats, such as
[record-jar](https://tools.ietf.org/html/draft-phillips-record-jar-01).
And let's be honest, ain't nobody excited about working with XML in 2021.

Each source is converted to JSON and written to a file in the `data/json/`
directory. Database aside, having up-to-date standards data in JSON format
may be useful in its own right.

### Load

```
make load
```

Finally, the `load/` module loads the data from the JSON files in `data/json/`
and inserts it into the database.

Once all data has been inserted, the database is dumped to `data/sql/`.

## Docker Image

The final
[Docker image](https://hub.docker.com/repository/docker/bryandragon/langdb)
is based on
[postgres:13-alpine](https://hub.docker.com/_/postgres)
and it can be built with the `make build` task.

To build and tag the latest Docker image after rebuilding the database,
run `make build`.

The Docker image contains the dump of the fully-loaded database and it is
seeded when the container starts up. Of course, you can always grab
the dump directly from `data/sql/` and do something else with it.

## Database Schema

The database schema is defined as a set of SQL migration files under
[load/migrations/](load/migrations/).

## Project Structure

| Directory | Description |
|---|---|
| `build/` | Dockerfiles and Bash scripts for each step. |
| `common/` | Library code used by the other modules. |
| `data/raw/` | Original, unprocessed source data. |
| `data/json/` | Source data as JSON. |
| `data/sql/` | SQL dump of the fully-loaded database. |
| `extract/` | Source code for the extract step. |
| `transform/` | Source code for the transform step. |
| `load/` | Source code for the load step. |
| `load/migrations/` | SQL schema for the database. |

## Development

Common development tasks are handled with Maven.

Install dependencies and validate the project:

```
mvn install validate
```

Run tests:

```
mvn test
```

Run tests for a specific module (for example, `transform`):

```
mvn -pl ./transform/ -am test
```

## Running Queries

Here is an example workflow that starts the database in one Docker container
and connects to it with a psql shell in a separate postgres Docker container
on the same Docker network.

```
docker network create langdb

# Start the database server in the background.
docker run -dit --network=langdb -h langdb -p 5432:5432 bryandragon/langdb

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

## License and Copyright

This project is free and open source. See the included [LICENSE](LICENSE).

This project only uses open source, publicly available data.
Any and all data downloaded from
[iana.org](https://iana.org/),
[iso.org](https://iso.org/),
[loc.gov](https://loc.gov/),
[iso639-3.sil.org](https://iso639-3.sil.org/),
[un.org](https://un.org/),
or [unicode.org](https://unicode.org/)
are bound by the copyright and terms and conditions of each.
