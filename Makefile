DOCKER_REPO ?= bryandragon
TOP_DIR := $(shell pwd)
LOGNAME := $(shell logname)
UID := $(shell id -u ${LOGNAME})
GID := $(shell id -g ${LOGNAME})

all: download transform
.PHONY: all

clean:
	docker rmi $(DOCKER_REPO)/langdb-download &>/dev/null || true
	docker rmi $(DOCKER_REPO)/langdb-transform &>/dev/null || true
	rm -rf $(TOP_DIR)/data/*
.PHONY: clean

# Builds the langdb-download Docker image.
build-download:
	docker build \
		--rm \
		-f $(TOP_DIR)/download/Dockerfile \
		-t $(DOCKER_REPO)/langdb-download \
		$(TOP_DIR)/download
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

# Builds the langdb-transform Docker image.
build-transform:
	docker build \
		--rm \
		-f $(TOP_DIR)/transform/Dockerfile \
		-t $(DOCKER_REPO)/langdb-transform \
		$(TOP_DIR)/transform
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
