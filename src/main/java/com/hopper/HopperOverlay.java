package com.hopper;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.util.ImageUtil;

@Singleton
public class HopperOverlay extends OverlayPanel
{
    private static final Color BG = new Color(20, 20, 25, 180);
    private static final Color FG_OK = new Color(120, 200, 120);
    private static final Color FG_WARN = new Color(255, 200, 50);
    private static final Color FG_CRIT = new Color(255, 80, 80);
    private static final Color FG_TEXT = new Color(220, 220, 225);
    private static final Color BAR_BG = new Color(60, 60, 70, 180);

    private final HopperPlugin plugin;
    private final HopperConfig config;
    private final BufferedImage icon;

    @Inject
    public HopperOverlay(HopperPlugin plugin, HopperConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(Overlay.PRIORITY_MED);

        panelComponent.setBackgroundColor(BG);

        icon = ImageUtil.loadImageResource(HopperOverlay.class, "hopper.png");
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        final int cap = config.capacity();
        final int rem = (int) plugin.getTokens();
        final int missing = Math.max(0, cap - rem);
        final int warn1 = config.warnAt1();
        final int warn2 = config.warnAt2();
        final Color accent = rem <= warn2 ? FG_CRIT : rem <= warn1 ? FG_WARN : FG_OK;

        if (config.showIcon() && icon != null)
        {
            panelComponent.getChildren().add(new ImageComponent(icon));
        }

        // Hops remaining
        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("Hops:")
                        .leftColor(FG_TEXT)
                        .right(rem + "/" + cap)
                        .rightColor(accent)
                        .build()
        );

        // Remaining bar
        ProgressBarComponent remBar = new ProgressBarComponent();
        remBar.setMinimum(0);
        remBar.setMaximum(cap);
        remBar.setValue(rem);
        remBar.setForegroundColor(accent);
        remBar.setBackgroundColor(BAR_BG);
        panelComponent.getChildren().add(remBar);

        // To full
        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("To full:")
                        .leftColor(FG_TEXT)
                        .right(missing + "/" + cap)
                        .rightColor(FG_TEXT)
                        .build()
        );

        // Time until full
        if (config.showTimeToFull())
        {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Full in:")
                            .leftColor(FG_TEXT)
                            .right(formatDuration(plugin.secondsToFull()))
                            .rightColor(FG_TEXT)
                            .build()
            );
        }

        return super.render(graphics);
    }

    private static String formatDuration(long totalSeconds)
    {
        if (totalSeconds <= 0) return "00:00";
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
