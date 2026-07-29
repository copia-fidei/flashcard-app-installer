#!/bin/bash
set -e

# Skript zum Entfernen und Neuinstallieren von PostgreSQL
# Verwendung: ./purge_and_install_postgres.sh <postgres_version>

if [ "$EUID" -ne 0 ]; then
    echo "Dieses Skript muss mit sudo ausgeführt werden."
    echo "Verwendung: sudo $0 <postgres_version>"
    exit 1
fi
if [ -z "$1" ]; then
    echo "Verwendung: $0 <postgres_version>"
    echo "Beispiel: $0 14"
    exit 1
fi

POSTGRES_VERSION=$1
PACKAGE_NAME="postgresql-${POSTGRES_VERSION}"

echo "Deaktiviere debconf für Datenbank-Cluster-Erhaltung..."


echo "Entferne PostgreSQL Installation..."
#printf "nein\n" | DEBIAN_FRONTEND=readline apt purge -y "${PACKAGE_NAME}"

# TODO might not works as a JAR
./apt_purge_postgres.exp "${PACKAGE_NAME}"
#DEBIAN_FRONTEND=readline apt purge -y "${PACKAGE_NAME}"

echo "PostgreSQL erfolgreich entfernt."

echo "Installiere PostgreSQL ${POSTGRES_VERSION}..."
apt install "${PACKAGE_NAME}"

echo "PostgreSQL ${POSTGRES_VERSION} erfolgreich installiert."
