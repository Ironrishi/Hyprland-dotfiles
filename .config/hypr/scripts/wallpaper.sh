#!/bin/bash
WALLPAPER_DIR="$HOME/Pictures/wallpapers"
CACHE="$HOME/.cache/thumbnails/current_wallpaper"
WALLPAPER=$(find "$WALLPAPER_DIR" -type f \( -name "*.jpg" -o -name "*.png" -o -name "*.jpeg" \) \
    | xargs -I{} basename {} \
    | wofi --dmenu --prompt "pick")
[[ -z "$WALLPAPER" ]] && { echo "No wallpapers found"; exit 1; }

echo "$WALLPAPER_DIR/$WALLPAPER" > "$CACHE"

awww img "$WALLPAPER_DIR/$WALLPAPER" \
  --transition-type any \
  --transition-duration 3 \
  --transition-fps 60 \
  --transition-angle 30
