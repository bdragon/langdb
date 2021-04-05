#!/bin/bash
set -euo pipefail

# Downloads sources.

DATA_DIR="${DATA_DIR:-/mnt/data}"

function download() {
    echo "Downloading $1"
    curl -sSLO "$1"
}

mkdir -p "$DATA_DIR/raw"
cd "$DATA_DIR/raw"

# ISO 639-3
download https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3.tab

# ISO 639-3: macrolanguages
download https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3-macrolanguages.tab

# ISO 639-3: name index
download https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3_Name_Index.tab

# ISO 639-3: retirements
download https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3_Retirements.tab

# ISO 639-2
download https://www.loc.gov/standards/iso639-2/ISO-639-2_utf-8.txt

# ISO 639-5
download http://id.loc.gov/vocabulary/iso639-5.tsv

# ISO 15924
download https://unicode.org/iso15924/iso15924.txt

# IANA Language Subtag Registry
download https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry

# IANA Language Tags Extension Registry
download https://www.iana.org/assignments/language-tag-extensions-registry/language-tag-extensions-registry

# Unicode CLDR
download http://www.unicode.org/Public/cldr/latest/core.zip
echo "Extracting <core.zip>/common/bcp47/* <core.zip>/common/dtd/ldmlBCP47.dtd"
mkdir -p cldr
rm -rf cldr/*
unzip -q core.zip "common/bcp47/*" "common/dtd/ldmlBCP47.dtd" -d cldr
rm core.zip

echo "Done."
