#!/bin/bash

vol=$(pamixer --get-volume)
mute=$(pamixer --get-mute)

if [ "$mute" = "true" ]; then
    echo "Muted"
else
    echo "$vol%"
fi

