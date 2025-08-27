package com.hopper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(HopperConfig.GROUP)
public interface HopperConfig extends Config
{
    String GROUP = "hopper";

    @ConfigItem(
            keyName = "showIcon",
            name = "Show icon",
            description = "Display the Hopper icon at the top of the overlay.",
            position = 0
    )
    default boolean showIcon() { return true; }

    @ConfigItem(
            keyName = "capacity",
            name = "Hop cap",
            description = "Maximum hops available (default 400).",
            position = 1
    )
    default int capacity() { return 400; }

    @ConfigItem(
            keyName = "regenPerHour",
            name = "Regen per hour",
            description = "Hops refreshed per hour (default 93).",
            position = 2
    )
    default int regenPerHour() { return 93; }

    @ConfigItem(
            keyName = "warnAt1",
            name = "Warn at",
            description = "First warning threshold for remaining hops.",
            position = 3
    )
    default int warnAt1() { return 50; }

    @ConfigItem(
            keyName = "warnAt2",
            name = "Critical at",
            description = "Critical warning threshold for remaining hops.",
            position = 4
    )
    default int warnAt2() { return 20; }

    @ConfigItem(
            keyName = "showTimeToFull",
            name = "Show time to full",
            description = "Display the time until you are back to full capacity.",
            position = 5
    )
    default boolean showTimeToFull() { return true; }

    default boolean resetOnLogin() { return false; }

    @ConfigItem(
            keyName = "resetHopsNow",
            name = "Reset now",
            description = "Click to instantly refill hops to your cap (auto-turns off after running).",
            position = 6
    )
    default boolean resetHopsNow() { return false; }
}
