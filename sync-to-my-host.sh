#!/bin/bash

REMOTE_USER="privactivity"
# Check if MY_HOST is set
if [ -z "$MY_HOST" ]; then
    echo "Error: MY_HOST environment variable is not set"
    echo "Usage: export MY_HOST=your-host-name && ./sync-to-my-host.sh"
    exit 1
fi

# Check if PRIVATE_DATA_DIR is set
if [ -z "$PRIVATE_DATA_DIR" ]; then
    echo "Error: PRIVATE_DATA_DIR environment variable is not set"
    echo "Usage: export PRIVATE_DATA_DIR=/path/to/your/private/data && ./sync-from-to-host.sh"
    exit 1
fi



echo "Syncing from $PRIVATE_DATA_DIR to $REMOTE_USER@$MY_HOST/$HOST_FIT_DIR"
rsync -avz -e ssh --exclude=.DS_Store --progress "$PRIVATE_DATA_DIR"/activities-fit/ "$REMOTE_USER"@"$MY_HOST":"$HOST_FIT_DIR"
