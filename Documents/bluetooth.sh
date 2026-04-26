#!/bin/bash

if ! bluetoothctl show | grep -q "Powered: yes"; then
    echo "Off"
    exit
fi

device=$(bluetoothctl info | grep "Name" | cut -d ' ' -f2-)

if [ -z "$device" ]; then
    echo "Disconnected"
else
    echo "$device"
fi
