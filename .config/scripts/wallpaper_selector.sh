
#  ██╗    ██╗ █████╗ ██╗     ██╗     ██████╗  █████╗ ██████╗ ███████╗██████╗
#  ██║    ██║██╔══██╗██║     ██║     ██╔══██╗██╔══██╗██╔══██╗██╔════╝██╔══██╗
#  ██║ █╗ ██║███████║██║     ██║     ██████╔╝███████║██████╔╝█████╗  ██████╔╝
#  ██║███╗██║██╔══██║██║     ██║     ██╔═══╝ ██╔══██║██╔═══╝ ██╔══╝  ██╔══██╗
#  ╚███╔███╔╝██║  ██║███████╗███████╗██║     ██║  ██║██║     ███████╗██║  ██║
#   ╚══╝╚══╝ ╚═╝  ╚═╝╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝     ╚══════╝╚═╝  ╚═╝
#
#  ██╗      █████╗ ██╗   ██╗███╗   ██╗ ██████╗██╗  ██╗███████╗██████╗
#  ██║     ██╔══██╗██║   ██║████╗  ██║██╔════╝██║  ██║██╔════╝██╔══██╗
#  ██║     ███████║██║   ██║██╔██╗ ██║██║     ███████║█████╗  ██████╔╝
#  ██║     ██╔══██║██║   ██║██║╚██╗██║██║     ██╔══██║██╔══╝  ██╔══██╗
#  ███████╗██║  ██║╚██████╔╝██║ ╚████║╚██████╗██║  ██║███████╗██║  ██║
#  ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
#	
#	Heavily inspired by:  develcooking - https://github.com/develcooking/hyprland-dotfiles	
# Info    - This script runs the rofi launcher, to select
#             the wallpapers included in the theme you are in.


#!/bin/bash

# Set some variables
wall_dir="${HOME}/Pictures/wallpapers/"
cache_dir="${HOME}/.cache/thumbnails/wal_selector"
rofi_config_path="${HOME}/.config/rofi/wallpaper-sel-config.rasi"
rofi_command="rofi -dmenu -config ${rofi_config_path}"

# Create cache dir if not exists
if [ ! -d "${cache_dir}" ]; then
    mkdir -p "${cache_dir}"
fi

# Pick whichever ImageMagick binary is available
if command -v magick &>/dev/null; then
    IM="magick"
elif command -v convert &>/dev/null; then
    IM="convert"
else
    echo "Error: ImageMagick not found. Install it with: sudo pacman -S imagemagick"
    exit 1
fi

# Replace the for loop with this:
while IFS= read -r -d '' imagen; do
    echo "Processing: $imagen"
    filename=$(basename "$imagen")
    if [ ! -f "${cache_dir}/${filename}" ]; then
      	  echo "Generating thumbnail for: $filename"
	  "$IM" -strip "$imagen" -thumbnail 500x500^ -gravity center -extent 500x500 "${cache_dir}/${filename}"
    fi
done < <(find "$wall_dir" -maxdepth 1 -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.webp" \) -print0)

# Select a picture with rofi
wall_selection=$(ls -t "$wall_dir" | while read -r A; do
    echo -en "$A\x00icon\x1f${cache_dir}/${A}\n"
done | ${rofi_command})

# Set the wallpaper with waypaper
[[ -n "$wall_selection" ]] || exit 1
waypaper --wallpaper "${wall_dir}${wall_selection}"
exit 0
