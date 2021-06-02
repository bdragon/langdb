DOCKER_REPO ?= bryandragon
TOP_DIR := $(shell pwd)
LOGNAME := $(shell logname)
UID := $(shell id -u ${LOGNAME})
GID := $(shell id -g ${LOGNAME})

all: download extract transform
.PHONY: all

clean:
	docker rmi $(DOCKER_REPO)/langdb-download &>/dev/null || true
	docker rmi $(DOCKER_REPO)/langdb-extract &>/dev/null || true
	docker rmi $(DOCKER_REPO)/langdb-transform &>/dev/null || true
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
#   * data/raw/iso15924.txt
#   * data/raw/ISO-639-2_utf-8.txt
#   * data/raw/iso-639-3-macrolanguages.tab
#   * data/raw/iso-639-3_Name_Index.tab
#   * data/raw/iso-639-3_Retirements.tab
#   * data/raw/iso639-5.tsv
#   * data/raw/language-subtag-registry
download: build-download
	docker run \
		--rm \
		-it \
		--user=$(UID):$(GID) \
		--volume=$(TOP_DIR)/data:/mnt/data \
		$(DOCKER_REPO)/langdb-download
.PHONY: download

# Builds the langdb-extract Docker image.
build-extract:
	docker build \
		--rm \
		-f $(TOP_DIR)/build/Dockerfile-extract \
		-t $(DOCKER_REPO)/langdb-extract \
		$(TOP_DIR)
.PHONY: build-extract

# Collects sources that do not have a download option:
#
#   * data/json/iso-3166-1.json
#   * data/json/iso-639-2-changes.json
#   * data/json/un-m49.json
extract: build-extract
	docker run \
		--rm \
		-it \
		--publish=4444:4444 \
		--user=$(UID):$(GID) \
		--volume=$(TOP_DIR)/data:/mnt/data \
		$(DOCKER_REPO)/langdb-extract
.PHONY: extract

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
