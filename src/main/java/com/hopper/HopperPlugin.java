package com.hopper;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WorldChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Hopper",
        description = "Tracks world hops remaining (cap 400) and regeneration (~93/hour).",
        tags = {"world", "hop", "limit", "tracker", "hopper"}
)
public class HopperPlugin extends Plugin
{
    private static final String CONFIG_GROUP = HopperConfig.GROUP;
    private static final String KEY_TOKENS = "stateTokens";
    private static final String KEY_LAST_UPDATE = "stateLastUpdate";

    @Inject private Client client;
    @Inject private HopperConfig config;
    @Inject private ConfigManager configManager;
    @Inject private OverlayManager overlayManager;
    @Inject private HopperOverlay overlay;
    @Inject private Notifier notifier;

    // Token bucket (for Hops remaining / Full in)
    private double tokens;
    private long lastUpdateMillis;

    // Notifications
    private boolean notifiedWarn1 = false;
    private boolean notifiedWarn2 = false;

    // hop detection
    private int stableWorld = -1;     // last known world when LOGGED_IN
    private boolean hopInProgress = false;
    private int lastWorldSeen = -1;

    // Latch so reset-on-login runs once per login
    private boolean loginResetApplied = false;

    @Provides
    HopperConfig provideConfig(ConfigManager cm)
    {
        return cm.getConfig(HopperConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);

        tokens = getDoubleConfig(KEY_TOKENS, config.capacity());
        lastUpdateMillis = getLongConfig(KEY_LAST_UPDATE, System.currentTimeMillis());

        updateTokens(System.currentTimeMillis());
        saveState();

        if (client != null && client.getGameState() == GameState.LOGGED_IN && client.getWorld() > 0)
        {
            stableWorld = client.getWorld();
            lastWorldSeen = stableWorld;
        }
        else
        {
            stableWorld = -1;
            lastWorldSeen = -1;
        }

        hopInProgress = false;
        notifiedWarn1 = false;
        notifiedWarn2 = false;
        loginResetApplied = false;
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        saveState();
        hopInProgress = false;
        stableWorld = -1;
        lastWorldSeen = -1;
        notifiedWarn1 = false;
        notifiedWarn2 = false;
        loginResetApplied = false;
    }

    /**
     * Pre-connection hint that a hop is starting; the actual decrement occurs after we hit LOGGED_IN.
     */
    @Subscribe
    public void onWorldChanged(WorldChanged e)
    {
        if (client != null && client.getWorld() > 0)
        {
            lastWorldSeen = client.getWorld();
            hopInProgress = true;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e)
    {
        final GameState state = e.getGameState();

        switch (state)
        {
            case HOPPING:
            case LOGGING_IN:
            case CONNECTION_LOST:
            case LOGIN_SCREEN:
                hopInProgress = true;
                loginResetApplied = false;
                break;

            case LOGGED_IN:
            {
                final int currentWorld = (client != null) ? client.getWorld() : -1;

                // First stable login (or after reconnect)
                if (stableWorld == -1 && currentWorld > 0)
                {
                    stableWorld = currentWorld;
                    lastWorldSeen = currentWorld;

                    // Apply one-time reset if the option is enabled
                    if (config.resetOnLogin() && !loginResetApplied)
                    {
                        doResetToFull();
                        loginResetApplied = true;
                    }

                    hopInProgress = false;
                    break;
                }

                // Count a hop only when arrived stably in a different world
                if (hopInProgress && currentWorld > 0 && currentWorld != stableWorld)
                {
                    handleHop(System.currentTimeMillis());
                    stableWorld = currentWorld;
                }

                hopInProgress = false;
                lastWorldSeen = currentWorld;
                break;
            }

            default:
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        updateTokens(System.currentTimeMillis());
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e)
    {
        if (!CONFIG_GROUP.equals(e.getGroup()))
        {
            return;
        }

        if ("resetHopsNow".equals(e.getKey()))
        {
            doResetToFull();

            configManager.setConfiguration(CONFIG_GROUP, "resetHopsNow", "false");
        }
        else if ("capacity".equals(e.getKey()))
        {
            tokens = Math.min(tokens, config.capacity());
            saveState();
        }
    }

    private void handleHop(long nowMillis)
    {
        updateTokens(nowMillis); // bring state up to now
        tokens = Math.max(0.0, tokens - 1.0); // spend one hop
        checkNotify();
        saveState();
    }

    private void updateTokens(long nowMillis)
    {
        if (nowMillis < lastUpdateMillis)
        {
            lastUpdateMillis = nowMillis; // clock moved backwards; clamp
            return;
        }

        final double ratePerMs = config.regenPerHour() / 3_600_000.0; // tokens per ms
        final long deltaMs = nowMillis - lastUpdateMillis;
        if (deltaMs <= 0) return;

        tokens = Math.min(config.capacity(), tokens + ratePerMs * deltaMs);
        lastUpdateMillis = nowMillis;

        final int rem = (int) Math.floor(tokens);
        if (rem > config.warnAt1()) notifiedWarn1 = false;
        if (rem > config.warnAt2()) notifiedWarn2 = false;
    }

    private void checkNotify()
    {
        final int rem = (int) Math.floor(tokens);
        if (!notifiedWarn1 && rem <= config.warnAt1())
        {
            notifier.notify("Hopper: Low hops remaining (" + rem + ")");
            notifiedWarn1 = true;
        }
        if (!notifiedWarn2 && rem <= config.warnAt2())
        {
            notifier.notify("Hopper: CRITICAL – very low hops remaining (" + rem + ")");
            notifiedWarn2 = true;
        }
    }

    private void doResetToFull()
    {
        tokens = config.capacity();
        lastUpdateMillis = System.currentTimeMillis();
        notifiedWarn1 = false;
        notifiedWarn2 = false;
        saveState();
    }

    private void saveState()
    {
        configManager.setConfiguration(CONFIG_GROUP, KEY_TOKENS, Double.toString(tokens));
        configManager.setConfiguration(CONFIG_GROUP, KEY_LAST_UPDATE, Long.toString(lastUpdateMillis));
    }

    private double getDoubleConfig(String key, double def)
    {
        try
        {
            String v = configManager.getConfiguration(CONFIG_GROUP, key);
            if (v == null) return def;
            return Double.parseDouble(v);
        }
        catch (Exception e)
        {
            return def;
        }
    }

    private long getLongConfig(String key, long def)
    {
        try
        {
            String v = configManager.getConfiguration(CONFIG_GROUP, key);
            if (v == null) return def;
            return Long.parseLong(v);
        }
        catch (Exception e)
        {
            return def;
        }
    }

    double getTokens()
    {
        return Math.max(0.0, Math.min(tokens, config.capacity()));
    }

    long secondsToFull()
    {
        final double missing = Math.max(0.0, config.capacity() - getTokens());
        final double perSecond = config.regenPerHour() / 3600.0;
        if (perSecond <= 0.0) return 0L;
        return (long) Math.ceil(missing / perSecond);
    }
}
