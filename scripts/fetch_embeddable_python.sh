#!/usr/bin/env bash
set -euo pipefail

PYTHON_RELEASE="3.14.7"
PYTHON_ABI="3.14"
variant=""
abi=""
out=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --variant) variant="$2"; shift 2 ;;
    --abi) abi="$2"; shift 2 ;;
    --out) out="$2"; shift 2 ;;
    *) echo "ERROR: unknown argument: $1" >&2; exit 2 ;;
  esac
done

if [[ -z "$variant" || -z "$abi" || -z "$out" ]]; then
  echo "ERROR: --variant, --abi, and --out are required" >&2
  exit 2
fi

if [[ "$variant" != "standard" ]]; then
  echo "ERROR: only the standard CPython runtime is approved for v0.1" >&2
  exit 3
fi

case "$abi" in
  arm64-v8a)
    archive_arch="aarch64"
    expected_sha256="6d50cc3aa66e414a439594089bcdfb5f1264358155c70c1f00471c24cfb477fb"
    ;;
  x86_64)
    archive_arch="x86_64"
    expected_sha256="2c16ce2359565cd8c24f86cfb75630768ba6607e732946b294b969797f583b60"
    ;;
  *)
    echo "ERROR: unsupported Android ABI: $abi" >&2
    exit 3
    ;;
esac

url="https://www.python.org/ftp/python/$PYTHON_RELEASE/python-$PYTHON_RELEASE-$archive_arch-linux-android.tar.gz"

rm -rf "$out"
mkdir -p "$out"
archive="$out/python-android.tar.gz"

curl --fail --location --retry 3 --retry-delay 2 "$url" --output "$archive"

actual_sha256="$(sha256sum "$archive" | awk '{print $1}')"
if [[ "$actual_sha256" != "$expected_sha256" ]]; then
  echo "ERROR: CPython Android archive SHA-256 mismatch" >&2
  echo "expected=$expected_sha256" >&2
  echo "actual=$actual_sha256" >&2
  exit 4
fi

tar -xzf "$archive" -C "$out"
rm -f "$archive"

prefix="$out/prefix"
for required in   "$prefix/lib/libpython$PYTHON_ABI.so"   "$prefix/include/python$PYTHON_ABI/Python.h"   "$prefix/lib/python$PYTHON_ABI"
do
  if [[ ! -e "$required" ]]; then
    echo "ERROR: official CPython package is missing required path: $required" >&2
    exit 5
  fi
done

cat > "$out/RUNTIME_MANIFEST.json" <<EOF
{
  "schemaVersion": 1,
  "status": "VERIFIED",
  "source": "python.org",
  "url": "$url",
  "pythonRelease": "$PYTHON_RELEASE",
  "pythonAbi": "$PYTHON_ABI",
  "variant": "$variant",
  "abi": "$abi",
  "sha256": "$expected_sha256",
  "prefix": "prefix"
}
EOF

echo "CPYTHON_RUNTIME_VERIFIED"
echo "prefix=$prefix"
