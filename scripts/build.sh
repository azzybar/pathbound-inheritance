#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "$0")/.."

NAME="Azrael Baram"
USER="azzybar"
YEAR="2026"
PROJECT="Pathbound: Inheritance"
DESCRIPTION="Pathbound: Inheritance - Originally a high school project made under 2 weeks"

APP_NAME="pathbound"
VERSION="0.1.0"
JAVA_VERSION="25"
MAIN_MODULE="dev.azzybar.pathbound"
MAIN_CLASS="${MAIN_MODULE}.Project"
JAR_NAME="pathbound-inheritance-${VERSION}.jar"

APP_DIR='-Dapp.dir=$APPDIR'
NATIVE_ACCESS="'--enable-native-access=javafx.graphics'"

SRC_DIR="src/java"
RES_DIR="src/resources"
RESOURCES=("items" "maps" "sprites")
BUILD_DIR="build"
CLASSES_DIR="${BUILD_DIR}/classes"
JAR_DIR="${BUILD_DIR}/jar"
INPUT_DIR="${BUILD_DIR}/input"
RUNTIME_DIR="${BUILD_DIR}/runtime"
DIST_DIR="${BUILD_DIR}/dist"
IMAGE_DIR="${DIST_DIR}/app-image"

ICON="${CLASSES_DIR}/styles/icons/icon.png"
LICENSES="LICENSE,NOTICE,legal"

JAVAFX_SDK="lib/javafx/sdk"
JAVAFX_JMODS="lib/javafx/jmods"

./scripts/clean.sh

echo "Compiling Java files..."
javac --release "${JAVA_VERSION}" \
      --module-path "${JAVAFX_SDK}" \
      -d "${CLASSES_DIR}" \
      "${SRC_DIR}/module-info.java" \
      "${SRC_DIR}/dev/azzybar/pathbound/"*.java \
      "${SRC_DIR}/dev/azzybar/pathbound/internal/"*.java
cp -r "${RES_DIR}/styles" "${CLASSES_DIR}/"

echo "Creating ${JAR_NAME}..."
jar --create \
    --file "${JAR_DIR}/${JAR_NAME}" \
    --module-version "${VERSION}" \
    --main-class "${MAIN_CLASS}" \
    -C "${CLASSES_DIR}" .

if [[ ! -d "${RUNTIME_DIR}" ]]; then
    echo "Building custom JRE for JavaFX..."
    jlink --module-path "${JAVA_HOME}/jmods:${JAVAFX_JMODS}" \
          --add-modules javafx.controls \
          --no-header-files \
          --no-man-pages \
          --strip-debug \
          --output "${RUNTIME_DIR}"
fi

for res in "${RESOURCES[@]}"; do
    if [[ -d "${RES_DIR}/${res}" ]]; then
        cp -r "${RES_DIR}/${res}" "${INPUT_DIR}/"
    fi
done

echo "Packaging ${PROJECT} (v${VERSION})..."
jpackage \
  --type app-image \
  --name "${APP_NAME}" \
  --app-version "${VERSION}" \
  --vendor "${USER}" \
  --description "${DESCRIPTION}" \
  --copyright "Copyright ${YEAR} ${NAME} (${USER})" \
  --icon "${ICON}" \
  --input "${INPUT_DIR}" \
  --app-content ${LICENSES} \
  --runtime-image "${RUNTIME_DIR}" \
  --module-path "${JAR_DIR}" \
  --module "${MAIN_MODULE}/${MAIN_CLASS}" \
  --java-options "${APP_DIR}" \
  --java-options "${NATIVE_ACCESS}" \
  --dest "${IMAGE_DIR}"

echo "Done. Launch with: ${IMAGE_DIR}/${APP_NAME}/bin/${APP_NAME}"
