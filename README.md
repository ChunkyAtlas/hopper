# Hopper

## Overview

**Hopper** is a plugin that tracks your world hops. By default, it supports a hop cap of 400 and regenerates about 93 hops per hour. The plugin provides a live overlay that shows your remaining hops, how many are needed to refill, and an estimated countdown until full capacity.

## Features

- **Hop Cap Tracking**  
  Displays your current number of hops remaining (e.g., `245/400`).

- **Live Countdown ("Full in")**  
  Uses `onGameTick` for a real-time timer that shows how long until your hops are fully regenerated.

- **Refill Progress**  
  A constantly updating "To full" line shows how far you are from full capacity (e.g., `155/400`).

- **Configurable Warnings**  
  Highlight remaining hops in yellow or red when thresholds are reached.

- **Manual Reset Button**  
  Click a single button in the config to instantly refill your hops to full.

- **Optional Reset on Login**  
  Automatically refills your hops upon login, if enabled.

- **Icon Toggle**  
  Show or hide the hopper icon above the overlay panel.

## Configuration

Go to RuneLite's plugin settings and configure **Hopper** options:

- **Show icon** – Display or hide the top icon.
- **Hop cap** – Set your maximum available hops (default: 400).
- **Regen per hour** – Adjust how many hops regenerate per hour (default: 93).
- **Warn at** – Threshold when a first warning is shown.
- **Critical at** – Threshold when a critical warning is shown.
- **Show time to full** – Toggle the display of the live countdown.
- **Reset now** – A button to instantly refill remaining hops.
- **Reset to full on login** – Automatically reset hops the next time you log in.

## Usage

1. Enable the **Hopper** in RuneLite’s sidebar.
2. Monitor the overlay at the top-left of your game:
    - **Hops** show current vs maximum.
    - **To full** indicates how many hops needed to refill.
    - **Full in** counts down until you're full (updates each tick).
3. Configure warnings and behavior via the settings panel.
4. Click **Reset now** to instantly refill to the cap.
5. Optionally, enable **Reset on login** to auto-refill when you log in.

## Contributing

Interested in improving Hopper?
1. Fork the repo.
2. Make your changes in a branch.
3. Submit a Pull Request with a detailed description of your changes.
4. Ensure your plugin follows RuneLite guidelines and compiles without error.

## Contact

For questions, support, or feature requests, please open an issue on GitHub or contact me at monstermonitor@proton.me
