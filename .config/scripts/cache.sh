#!/bin/bash

wall_dir="${HOME}/Pictures/wallpapers/"
cache_dir="${HOME}/.cache/thumbnails/wal_selector"

mkdir -p "${cache_dir}"

# Detect ImageMagick binary
if command -v magick &>/dev/null; then
    IM="magick"
elif command -v convert &>/dev/null; then
    IM="convert"
else
    echo "Error: ImageMagick not found. Install it with: sudo pacman -S imagemagick"
    exit 1
fi

total=0
success=0
fail=0

while IFS= read -r -d '' imagen; do
    filename=$(basename "$imagen")
    thumb="${cache_dir}/${filename}"
    total=$((total + 1))

    if [ -f "$thumb" ]; then
        echo "  [skip] $filename"
        continue
    fi

    echo -n "  [gen]  $filename ... "
    if "$IM" "$imagen" -strip -thumbnail 500x500^ -gravity center -extent 500x500 "$thumb" 2>/dev/null; then
        echo "ok"
        success=$((success + 1))
    else
        echo "FAILED"
        fail=$((fail + 1))
    fi

done < <(find "$wall_dir" -maxdepth 1 -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.webp" \) -print0)

echo ""
echo "Done. Total: $total | Generated: $success | Failed: $fail | Skipped: $((total - success - fail))"
