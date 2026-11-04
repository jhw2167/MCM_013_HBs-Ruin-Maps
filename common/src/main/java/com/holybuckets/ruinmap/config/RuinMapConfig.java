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

    @Comment("Structures a Common Ruin Map may target, as structure_id=icon_id. Common icon ids are 100-115 on the Easy Maps icon sheet, omit =icon_id to use 100. Minecraft namespace assumed when omitted")
    @NestedType(String.class)
    public List<String> commonStructures = new ArrayList<>(Arrays.asList(
        "minecraft:village_plains=200",
        "minecraft:village_desert=201",
        "minecraft:village_savanna=202",
        "minecraft:village_snowy=203",
        "minecraft:village_taiga=204",
        "minecraft:pillager_outpost=205"
    ));

    @Comment("Structures a Rare Ruin Map may target, as structure_id=icon_id. Rare icon ids are 200-215, omit =icon_id to use 200")
    @NestedType(String.class)
    public List<String> rareStructures = new ArrayList<>(Arrays.asList(
        "minecraft:desert_pyramid=300",
        "minecraft:jungle_pyramid=301"
    ));

    @Comment("Structures an Epic Ruin Map may target, as structure_id=icon_id. Epic icon ids are 300-315, omit =icon_id to use 300")
    @NestedType(String.class)
    public List<String> epicStructures = new ArrayList<>(Arrays.asList(
        "minecraft:stronghold=400",
        "minecraft:monument=401",
        "minecraft:trail_ruins=402"
    ));

    @Comment("Structures a Legendary Ruin Map may target, as structure_id=icon_id. Legendary icon ids are 400-415, omit =icon_id to use 400")
    @NestedType(String.class)
    public List<String> legendaryStructures = new ArrayList<>(Arrays.asList(
        "minecraft:mansion=403",
        "minecraft:ancient_city=404"
    ));

}
