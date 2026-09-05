#!/usr/bin/env bash
set -euo pipefail

variant=""
abi=""
out=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --variant)
      variant="$2"
      shift 2
      ;;
    --abi)
      abi="$2"
      shift 2
      ;;
    --out)
      out="$2"
      shift 2
      ;;
    *)
      echo "ERROR: unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

if [[ -z "$variant" || -z "$abi" || -z "$out" ]]; then
  echo "ERROR: --variant, --abi, and --out are required" >&2
  exit 2
fi

mkdir -p "$out"

cat > "$out/RUNTIME_STATUS.json" <<EOF
{
  "schemaVersion": 1,
  "status": "UNRESOLVED",
  "pythonVersion": "3.14",
  "variant": "$variant",
  "abi": "$abi",
  "reason": "No verified direct CPython Android runtime source has been configured yet"
}
EOF

echo "CPYTHON_RUNTIME_UNRESOLVED"
echo "status=$out/RUNTIME_STATUS.json"
exit 2
