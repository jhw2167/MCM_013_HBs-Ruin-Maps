package com.holybuckets.ruinmap.config;

import com.holybuckets.ruinmap.Constants;
import net.blay09.mods.balm.api.config.reflection.Comment;
import net.blay09.mods.balm.api.config.reflection.Config;
import net.blay09.mods.balm.api.config.reflection.NestedType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Config(Constants.MOD_ID)
public class RuinMapConfig {

    @Comment("devMode==true logs every structure pool hydration result on server start")
    public boolean devMode = false;

    @Comment("Structure ids a Common Ruin Map may target. Namespaced ids, minecraft namespace assumed when omitted")
    @NestedType(String.class)
    public List<String> commonStructures = new ArrayList<>(Arrays.asList(
        "minecraft:village_plains",
        "minecraft:village_desert",
        "minecraft:village_savanna",
        "minecraft:village_snowy",
        "minecraft:village_taiga",
        "minecraft:pillager_outpost"
    ));

    @Comment("Structure ids a Rare Ruin Map may target")
    @NestedType(String.class)
    public List<String> rareStructures = new ArrayList<>(Arrays.asList(
        "minecraft:desert_pyramid",
        "minecraft:jungle_pyramid"
    ));

    @Comment("Structure ids an Epic Ruin Map may target")
    @NestedType(String.class)
    public List<String> epicStructures = new ArrayList<>(Arrays.asList(
        "minecraft:stronghold",
        "minecraft:monument",
        "minecraft:trail_ruins"
    ));

    @Comment("Structure ids a Legendary Ruin Map may target")
    @NestedType(String.class)
    public List<String> legendaryStructures = new ArrayList<>(Arrays.asList(
        "minecraft:mansion",
        "minecraft:ancient_city"
    ));

}
