#!/bin/sh
JDK_VERSION='21'
INSTALL_DIR="$HOME/Applications"

while getopts ':v:' OPTION; do
  case "$OPTION" in 
    v) 
      JDK_VERSION=${OPTARG}
      echo "JDK requested is ${JDK_VERSION}"
      shift "$(($OPTIND -1))"
      ;;
    ?) 
      # echo "Usage: $(basename $0) [-v] <version> [-jdk]"
      ;;
  esac
done

JDK_URI="https://api.adoptium.net/v3/binary/latest/${JDK_VERSION}/ga/linux/x64/jdk/hotspot/normal/eclipse"
JDK_STRING="project=jdk"
RELEASE_INFO="https://api.adoptium.net/v3/assets/feature_releases/${JDK_VERSION}/ga/"
RELEASE_STRING="architecture=x64&heap_size=normal&image_type=jdk&os=linux&page=0&page_size=10&project=jdk&sort_method=DEFAULT&sort_order=DESC&vendor=eclipse"

# download jdk
TMPFILE=`mktemp`
TMPJSON=`mktemp`

echo "Downloading ${JDK_URI}..."
curl -G -H 'accept: */*' -L "${JDK_URI}" -d "${JDK_STRING}" > "${TMPFILE}" 
# query versions
curl -G -H 'accept: application/json' -L "${RELEASE_INFO}" -d "${RELEASE_STRING}" > "${TMPJSON}"

# checksums
EXPECTED_SHA256=`jq -r '.[0].binaries[0].package.checksum' "${TMPJSON}"`
REAL_SHA256=`shasum -a 256 "${TMPFILE}" |cut -d " " -f 1`
echo "Expected SHA256 checksum: ${EXPECTED_SHA256}"
echo "File SHA256 checksum:     ${REAL_SHA256}"

if [ "${EXPECTED_SHA256}" != "${REAL_SHA256}" ]; then
        echo "Cleaning up..."
        rm "${TMPFILE}"
        echo "Checksum mismatch (using URI below)! Exiting."
        echo "$RELEASE_INFO"
        exit
fi

# unpack to install directory
RELEASE_NAME=`jq -r '.[0].release_name' ${TMPJSON}`

echo "Unpacking ${RELEASE_NAME}"
rm -rf "${INSTALL_DIR}/${RELEASE_NAME}"
tar -C "${INSTALL_DIR}" -xvzf "${TMPFILE}" >/dev/null 2>&1

# recreate symlink
cd ${INSTALL_DIR}
rm jdk-${JDK_VERSION}.jdk
ln -s ${RELEASE_NAME} jdk-${JDK_VERSION}.jdk
