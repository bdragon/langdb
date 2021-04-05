DOCKER_REPO ?= bryandragon
TOP_DIR := $(shell pwd)
LOGNAME := $(shell logname)
UID := $(shell id -u ${LOGNAME})
GID := $(shell id -g ${LOGNAME})

all: download transform load build
.PHONY: all

clean:
	docker rmi $(DOCKER_REPO)/langdb-download &>/dev/null || true
	docker rmi $(DOCKER_REPO)/langdb-transform &>/dev/null || true
	docker rmi $(DOCKER_REPO)/langdb-load &>/dev/null || true
	docker rmi $(DOCKER_REPO)/langdb &>/dev/null || true
	rm -rf $(TOP_DIR)/data/*
.PHONY: clean

# Builds the langdb-download Docker image.
build-download:
	docker build \
		--rm \
		-f $(TOP_DIR)/build/Dockerfile-download \
		-t $(DOCKER_REPO)/langdb-download \
		$(TOP_DIR)
.PHONY: build-download

# Downloads sources.
#
#   * data/raw/cldr/common/bcp47/*.xml
#   * data/raw/cldr/common/dtd/ldmlBCP47.dtd
#   * data/raw/iso15924.txt
#   * data/raw/ISO-639-2_utf-8.txt
#   * data/raw/iso-639-3-macrolanguages.tab
#   * data/raw/iso-639-3_Name_Index.tab
#   * data/raw/iso-639-3_Retirements.tab
#   * data/raw/iso639-5.tsv
#   * data/raw/language-subtag-registry
#   * data/raw/language-tag-extensions-registry
download: build-download
	docker run \
		--rm \
		-it \
		--user=$(UID):$(GID) \
		--volume=$(TOP_DIR)/data:/mnt/data \
		$(DOCKER_REPO)/langdb-download
.PHONY: download

# Builds the langdb-transform Docker image.
build-transform:
	docker build \
		--rm \
		-f $(TOP_DIR)/build/Dockerfile-transform \
		-t $(DOCKER_REPO)/langdb-transform \
		$(TOP_DIR)
.PHONY: build-transform

# Converts raw sources to JSON format.
transform: build-transform
	docker run \
		--rm \
		-it \
		--user=$(UID):$(GID) \
		--volume=$(TOP_DIR)/data:/mnt/data \
		$(DOCKER_REPO)/langdb-transform
.PHONY: transform

# Builds the langdb-load Docker image.
build-load:
	docker build \
		--rm \
		-f $(TOP_DIR)/build/Dockerfile-load \
		-t $(DOCKER_REPO)/langdb-load \
		$(TOP_DIR)
.PHONY: build-load

# Seeds the database and dumps it to data/sql/langdb.sql. It works as follows:
#
# 1. Starts a temporary postgres container with load/migrations/ and
#    data/ mounted as volumes. The database schema is created automatically
#    when the container starts.
# 2. Starts the langdb-load container (with data/ mounted as a volume),
#    which connects to the the temporary postgres container and populates
#    the database.
# 3. Invokes pg_dump in the running temporary postgres container to dump
#    the fully-seeded database as a SQL file in the mounted data/ volume.
load: build-load
	set -eu ;\
	docker network create langdb-load &>/dev/null || true ;\
	POSTGRES_CID=$$( \
		docker run \
			--rm \
			-dt \
			--network=langdb-load \
			--hostname=postgres \
			--volume=$(TOP_DIR)/data:/mnt/data \
			--volume=$(TOP_DIR)/load/migrations:/docker-entrypoint-initdb.d \
			-e POSTGRES_DB=langdb \
			-e POSTGRES_USER=langdb \
			-e POSTGRES_PASSWORD=langdb \
			postgres:13 \
	) ;\
	sleep 10 ;\
	docker run \
		--rm \
		-it \
		--network=langdb-load \
		--volume=$(TOP_DIR)/data:/mnt/data \
		-e PGHOST=postgres \
		-e PGDATABASE=langdb \
		-e PGUSER=langdb \
		-e PGPASSWORD=langdb \
		$(DOCKER_REPO)/langdb-load ;\
	docker exec -it --user=$(UID):$(GID) $$POSTGRES_CID \
		pg_dump \
			--verbose \
			--clean \
			--no-comments \
			--if-exists \
			--column-inserts \
			--file=/mnt/data/sql/langdb.sql \
			--encoding=utf8 \
			--username=langdb \
			langdb ;\
	docker kill -s SIGTERM $$POSTGRES_CID ;\
	sleep 3 ;\
	docker network rm langdb-load # &>/dev/null || true
.PHONY: load

# Builds the langdb Docker image, copying data/sql/langdb.sql
# to /docker-entrypoint-initdb.d/ so that it will be applied when the
# container starts.
build:
	docker build \
		--rm \
		-f $(TOP_DIR)/build/Dockerfile \
		-t $(DOCKER_REPO)/langdb \
		$(TOP_DIR)
.PHONY: build
