# Hyprland Dotfiles

My personal Arch Linux dotfiles for a Hyprland-based setup.

![Hyprland](https://img.shields.io/badge/WM-Hyprland-blue) ![Arch](https://img.shields.io/badge/OS-Arch_Linux-1793D1)

## Setup

| Component | App |
|-----------|-----|
| Compositor | [Hyprland](https://hyprland.org/) |
| Bar | [Waybar](https://github.com/Alexays/Waybar) |
| Launcher | [Rofi](https://github.com/davatorium/rofi) |
| File Manager | [Thunar](https://docs.xfce.org/xfce/thunar/start) |
| Notification Daemon | [Dunst](https://dunst-project.org/) |
| Shell | Zsh + Oh My Zsh |

## Screenshots

![desktop](screenshots/ss1.jpg)

**rofi**
![rofi](screenshots/ss2.jpg)

**yazi**
![yazi](screenshots/ss3.jpg)


## Installation

### 1. Install packages

```bash
sudo pacman -S --needed - < packages.txt
```

For AUR packages (using yay):

```bash
yay -S --needed - < aur-packages.txt
```

### 2. Clone the repo

```bash
git clone https://github.com/Ironrishi/Hyprland-dotfiles.git ~/Dotfiles
cd ~/Dotfiles
```

### 3. Copy configs

```bash
cp -r .config/* ~/.config/
cp .zshrc ~/
cp -r .zsh ~/
cp -r .oh-my-zsh ~/
cp -r wallpapers ~/Pictures/
```

### 4. Set up GRUB (if dual booting)

```bash
sudo cp grub /etc/default/grub
sudo grub-mkconfig -o /boot/grub/grub.cfg
```

### 5. Set up fstab (for drive mounts)

```bash
# Review first before copying!
cat fstab
sudo cp fstab /etc/fstab
```

### 6. Reload Hyprland

Log out and back in, or press `Super + Shift + E` to exit and relaunch.

## Structure

```
Dotfiles/
├── .config/
│   ├── hypr/        # Hyprland config
│   ├── waybar/      # Waybar config & styles
│   ├── rofi/        # Rofi themes
│   └── ...
├── .zsh/            # Zsh plugins/config
├── .oh-my-zsh/      # Oh My Zsh
├── .zshrc           # Zsh rc
├── wallpapers/      # Wallpapers
├── packages.txt     # Pacman packages
├── aur-packages.txt # AUR packages
├── grub             # GRUB config
└── fstab            # Drive mounts
```

## Notes

- Windows is mounted at `/mnt` — check `fstab` before copying it on a new install, partition UUIDs may differ.
- Wallpapers are in `~/Pictures/wallpapers/`.
