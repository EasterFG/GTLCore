package org.gtlcore.gtlcore.config;

import org.gtlcore.gtlcore.GTLCore;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GTLCore.MOD_ID)
public class ConfigHolder {

    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }

    @Configurable
    public boolean disableDrift = true;
    @Configurable
    public boolean enableSkyBlokeMode = false;
    @Configurable
    @Configurable.Range(min = 1)
    public int oreMultiplier = 4;
    @Configurable
    @Configurable.Range(min = 1)
    public int cellType = 4;
    @Configurable
    @Configurable.Range(min = 1)
    public int spacetimePip = Integer.MAX_VALUE;
    @Configurable
    @Configurable.Range(min = 0)
    public double durationMultiplier = 1;
    @Configurable
    @Configurable.Range(min = 1)
    public int travelStaffCD = 2;
    @Configurable
    @Configurable.Comment({ "更大的数值会让界面显示有问题，推荐在样板管理终端管理" })
    @Configurable.Range(min = 36, max = 360)
    public int exPatternProvider = 36;
    @Configurable
    public boolean enablePrimitiveVoidOre = false;
    @Configurable
    @Configurable.Comment("连锁黑名单,支持通配符*")
    @Configurable.Synchronized
    public String[] blackBlockList = { "ae2:cable_bus", "minecraft:grass_block" };

    @Configurable
    public String[] mobList1 = new String[] { "chicken", "rabbit", "sheep", "cow", "horse", "pig", "donkey", "skeleton_horse", "iron_golem", "wolf", "goat", "parrot", "camel", "cat", "fox", "llama", "panda", "polar_bear" };
    @Configurable
    public String[] mobList2 = new String[] { "ghast", "zombie", "pillager", "zombie_villager", "skeleton", "drowned", "witch", "spider", "creeper", "husk", "wither_skeleton", "blaze", "zombified_piglin", "slime", "vindicator", "enderman" };
}
