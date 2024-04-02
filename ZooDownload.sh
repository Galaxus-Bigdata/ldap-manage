#!/bin/bash

# Array of znodes to download
znodes=(
    "/path/to/znode1"
    "/path/to/znode2"
    "/path/to/znode3"
)

# Loop through the array of znodes
for znode in "${znodes[@]}"
do
    echo "Downloading data from $znode ..."
    data=$(echo "get $znode" | ./zkCli.sh -server <ZooKeeperServerHostname>:<ZooKeeperServerPort> 2>/dev/null)
    echo "$data" > "$(basename $znode).txt"
    echo "Downloaded data from $znode to $(basename $znode).txt"
done
