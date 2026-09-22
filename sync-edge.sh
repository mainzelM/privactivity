#!/usr/bin/env bash

set -euo pipefail

# Check if PRIVATE_DATA_DIR is set
if [ -z "$PRIVATE_DATA_DIR" ]; then
    echo "Error: PRIVATE_DATA_DIR environment variable is not set"
    echo "Usage: export PRIVATE_DATA_DIR=/path/to/your/private/data && ./sync-from-my-host.sh"
    exit 1
fi
DEST_DIR=$PRIVATE_DATA_DIR/activities-fit
MIN_FILE_ID=311
MAX_RETRIES=5

mkdir -p "$DEST_DIR"

echo "Reading activity list from Garmin..."

read_filetree() {
    local filetree
    local exit_status

    while true; do
        exit_status=0
        filetree="$(mtp-filetree 2>&1)" || exit_status=$?

        if [[ "$filetree" == *"No raw devices found."* ]]; then
            echo "Garmin is not connected yet. Retrying in 1 second..."
            sleep 1
            continue
        fi

        if (( exit_status != 0 )); then
            printf '%s\n' "$filetree" >&2
            return "$exit_status"
        fi

        printf '%s\n' "$filetree"
        return 0
    done
}

download_file() {
    local file_id="$1"
    local destination="$2"
    local filename="$3"
    local attempt=1

    while (( attempt <= MAX_RETRIES )); do
        echo "Downloading: $filename (ID $file_id, attempt $attempt/$MAX_RETRIES)"

        if mtp-getfile "$file_id" "$destination"; then
            return 0
        fi

        echo "Download failed. Waiting before retrying..."
        rm -f "$destination"
        sleep 2

        ((attempt++))
    done

    echo "ERROR: Could not download $filename after $MAX_RETRIES attempts." >&2
    return 1
}

read_filetree |
awk '
/^  [0-9]+ Activities$/ { in_activities=1; next }
in_activities && /^  [0-9]+ / { exit }
in_activities && /\.fit$/ { print $1, $2 }
' |
while read -r FILE_ID FILE_NAME; do
    # Skip old MTP file IDs
    if (( FILE_ID < MIN_FILE_ID )); then
        echo "Skipping old ID $FILE_ID: $FILE_NAME"
        continue
    fi

    DEST_FILE="$DEST_DIR/$FILE_NAME"

    if [[ -f "$DEST_FILE" ]]; then
        echo "Skipping existing: $FILE_NAME"
        continue
    fi

    download_file "$FILE_ID" "$DEST_FILE" "$FILE_NAME"

    # Give the Garmin a moment to release/reinitialize its MTP connection.
    sleep 1
done

echo "Sync complete."