#!/bin/bash
if [ "$1" = "pick" ]; then
    chosen=$(printf "performance\nbalanced\npower-saver" | wofi --dmenu --prompt "Power Profile")
    if [ -n "$chosen" ]; then
        powerprofilesctl set "$chosen"
        pkill -RTMIN+8 waybar
    fi
else
    profile=$(powerprofilesctl get)
    case $profile in
        performance)  echo " " ;;
        balanced)     echo "󱐋 " ;;
        power-saver)  echo "󰊠 " ;;
        *)            echo "? $profile" ;;
    esac
fi
