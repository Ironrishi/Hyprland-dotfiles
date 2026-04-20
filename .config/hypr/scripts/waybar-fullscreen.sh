#!/bin/bash

SOCKET="${XDG_RUNTIME_DIR}/hypr/${HYPRLAND_INSTANCE_SIGNATURE}/.socket2.sock"

handle() {
  if [[ "$1" == fullscreen* ]]; then
    STATUS="${1##*>>}"
    if [ "$STATUS" = "1" ]; then
      pkill -SIGUSR1 waybar
    else
      pkill -SIGUSR2 waybar
    fi
  fi
}

socat - UNIX-CONNECT:"$SOCKET" | while read -r line; do
  handle "$line"
done
