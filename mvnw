#!/usr/bin/env sh
set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
PROPS="$WRAPPER_DIR/maven-wrapper.properties"
DIST_URL="$(sed -n 's/^distributionUrl=//p' "$PROPS")"
MAVEN_VERSION="$(printf '%s' "$DIST_URL" | sed -n 's#.*/apache-maven-\([0-9.]*\)-bin.zip#\1#p')"
MAVEN_HOME="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION"
ZIP_FILE="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION-bin.zip"
JDK_HOME="$WRAPPER_DIR/jdk-17.0.19+10"
JDK_FILE="$WRAPPER_DIR/jdk-17.0.19+10.tar.gz"
JDK_URL="https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk"

download() {
  url="$1"
  output="$2"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$url" -o "$output"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$url" -O "$output"
  else
    echo "curl or wget is required to download build tools" >&2
    exit 1
  fi
}

if ! "${JAVA_HOME:-}/bin/javac" --release 17 -version >/dev/null 2>&1 && ! javac --release 17 -version >/dev/null 2>&1; then
  mkdir -p "$WRAPPER_DIR"
  if [ ! -x "$JDK_HOME/bin/javac" ]; then
    download "$JDK_URL" "$JDK_FILE"
    tar -xzf "$JDK_FILE" -C "$WRAPPER_DIR"
  fi
  export JAVA_HOME="$JDK_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$WRAPPER_DIR"
  download "$DIST_URL" "$ZIP_FILE"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$ZIP_FILE" -d "$WRAPPER_DIR"
  else
    echo "unzip is required to extract Maven" >&2
    exit 1
  fi
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
