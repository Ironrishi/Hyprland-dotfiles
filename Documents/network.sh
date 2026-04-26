#!/bin/bash

wifi=$(nmcli -t -f active,ssid dev wifi | grep '^yes' | cut -d: -f2)

if [ -z "$wifi" ]; then
    echo "Disconnected"
else
    echo "$wifi"
fi
