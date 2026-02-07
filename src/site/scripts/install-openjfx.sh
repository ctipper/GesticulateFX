#!/bin/sh
# install-openjfx.sh — Download, verify, and install OpenJFX SDK + jmods from Gluon
# POSIX sh compatible

set -eu

# ── Usage / option parsing ─────────────────────────────────────────────────
usage() {
    printf 'Usage: %s [-v version]\n' "$(basename "$0")" >&2
    exit 1
}

VERSION="25.0.2"
while getopts "v:" opt; do
    case $opt in
        v) VERSION="$OPTARG" ;;
        *) usage ;;
    esac
done
shift $((OPTIND - 1))

# ── Configuration ──────────────────────────────────────────────────────────
ARCH="linux-x64"
BASE_URL="https://download2.gluonhq.com/openjfx"
INSTALL_DIR="${HOME}/Applications"

SDK_FILE="openjfx-${VERSION}_${ARCH}_bin-sdk.zip"
JMODS_FILE="openjfx-${VERSION}_${ARCH}_bin-jmods.zip"
SDK_URL="${BASE_URL}/${VERSION}/${SDK_FILE}"
JMODS_URL="${BASE_URL}/${VERSION}/${JMODS_FILE}"
SDK_SHA_URL="${SDK_URL}.sha256"
JMODS_SHA_URL="${JMODS_URL}.sha256"

EXTRACT_DIR="javafx-sdk-${VERSION}"
JMODS_EXTRACT_DIR="javafx-jmods-${VERSION}"
SYMLINK_NAME="javafx-sdk"

TMPDIR="${TMPDIR:-/tmp}"
WORK_DIR="${TMPDIR}/openjfx-install-$$"

# ── Helpers ────────────────────────────────────────────────────────────────
die() {
    printf 'ERROR: %s\n' "$1" >&2
    exit 1
}

cleanup() {
    rm -rf "${WORK_DIR}"
}
trap cleanup EXIT INT TERM

# Verify a file against a Gluon .sha256 sidecar file.
# Usage: verify_sha256 <file> <sha256_file>
verify_sha256() {
    _file="$1"
    _sha_file="$2"
    _expected=$(awk '{print $1}' "$_sha_file")
    _actual=$(sha256sum "$_file" | awk '{print $1}')
    if [ "$_expected" != "$_actual" ]; then
        printf 'SHA-256 MISMATCH for %s\n' "$(basename "$_file")" >&2
        printf '  Expected: %s\n' "$_expected" >&2
        printf '  Actual:   %s\n' "$_actual" >&2
        return 1
    fi
    printf 'SHA-256 OK  %s  (%s)\n' "$(basename "$_file")" "$_actual"
    return 0
}

# ── Dependency checks ─────────────────────────────────────────────────────
for cmd in curl sha256sum unzip; do
    command -v "$cmd" >/dev/null 2>&1 || die "'$cmd' is required but not found"
done

# ── Create working directory ──────────────────────────────────────────────
mkdir -p "${WORK_DIR}"

# ── Parallel download of SDK, jmods, and their checksums ──────────────────
# curl -Z (--parallel) downloads all URLs concurrently in a single process.
printf 'Downloading SDK, jmods, and checksums in parallel ...\n'
curl -fSL -Z \
    -o "${WORK_DIR}/${SDK_FILE}"          "${SDK_URL}" \
    -o "${WORK_DIR}/${SDK_FILE}.sha256"   "${SDK_SHA_URL}" \
    -o "${WORK_DIR}/${JMODS_FILE}"        "${JMODS_URL}" \
    -o "${WORK_DIR}/${JMODS_FILE}.sha256" "${JMODS_SHA_URL}" \
    || die "One or more downloads failed"

# ── Verify checksums ─────────────────────────────────────────────────────
verify_sha256 "${WORK_DIR}/${SDK_FILE}"   "${WORK_DIR}/${SDK_FILE}.sha256" \
    || die "SDK SHA-256 verification FAILED"
verify_sha256 "${WORK_DIR}/${JMODS_FILE}" "${WORK_DIR}/${JMODS_FILE}.sha256" \
    || die "jmods SHA-256 verification FAILED"

# ── Prepare install directory ─────────────────────────────────────────────
mkdir -p "${INSTALL_DIR}"

# Remove a previous install of the same version if present
if [ -d "${INSTALL_DIR}/${EXTRACT_DIR}" ]; then
    printf 'Removing previous %s install ...\n' "${EXTRACT_DIR}"
    rm -rf "${INSTALL_DIR:?}/${EXTRACT_DIR}"
fi

# ── Unpack SDK ────────────────────────────────────────────────────────────
printf 'Unpacking SDK to %s/%s ...\n' "${INSTALL_DIR}" "${EXTRACT_DIR}"
unzip -q "${WORK_DIR}/${SDK_FILE}" -d "${INSTALL_DIR}" \
    || die "Failed to unzip ${SDK_FILE}"

[ -d "${INSTALL_DIR}/${EXTRACT_DIR}" ] \
    || die "Expected directory ${EXTRACT_DIR} not found after unzip"

# ── Unpack jmods into the SDK directory ───────────────────────────────────
# The jmods zip extracts to javafx-jmods-<version>/.  We unpack to the temp
# working directory then move the contents into <sdk>/jmods.
printf 'Unpacking jmods ...\n'
unzip -q "${WORK_DIR}/${JMODS_FILE}" -d "${WORK_DIR}" \
    || die "Failed to unzip ${JMODS_FILE}"

[ -d "${WORK_DIR}/${JMODS_EXTRACT_DIR}" ] \
    || die "Expected directory ${JMODS_EXTRACT_DIR} not found after unzip"

JMODS_DEST="${INSTALL_DIR}/${EXTRACT_DIR}/jmods"
rm -rf "${JMODS_DEST}"
mv "${WORK_DIR}/${JMODS_EXTRACT_DIR}" "${JMODS_DEST}" \
    || die "Failed to move jmods into SDK directory"

printf 'jmods installed to %s\n' "${JMODS_DEST}"

# ── Create / recreate symbolic link ───────────────────────────────────────
LINK_PATH="${INSTALL_DIR}/${SYMLINK_NAME}"

# ln -sfn atomically replaces an existing symlink (or removes a dangling one)
ln -sfn "${EXTRACT_DIR}" "${LINK_PATH}" \
    || die "Failed to create symlink ${LINK_PATH} -> ${EXTRACT_DIR}"

printf 'Symlink: %s -> %s\n' "${LINK_PATH}" "${EXTRACT_DIR}"

# ── Done ──────────────────────────────────────────────────────────────────
printf '\nOpenJFX %s installed successfully.\n' "${VERSION}"
printf 'SDK path:   %s/%s\n' "${INSTALL_DIR}" "${SYMLINK_NAME}"
printf 'jmods path: %s/%s/jmods\n' "${INSTALL_DIR}" "${SYMLINK_NAME}"
