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
        "minecraft:village_plains=100",
        "minecraft:village_desert=101",
        "minecraft:village_savanna=102",
        "minecraft:village_snowy=103",
        "minecraft:village_taiga=104",
        "minecraft:pillager_outpost=105"
    ));

    @Comment("Structures a Rare Ruin Map may target, as structure_id=icon_id. Rare icon ids are 200-215, omit =icon_id to use 200")
    @NestedType(String.class)
    public List<String> rareStructures = new ArrayList<>(Arrays.asList(
        "minecraft:desert_pyramid=200",
        "minecraft:jungle_pyramid=201"
    ));

    @Comment("Structures an Epic Ruin Map may target, as structure_id=icon_id. Epic icon ids are 300-315, omit =icon_id to use 300")
    @NestedType(String.class)
    public List<String> epicStructures = new ArrayList<>(Arrays.asList(
        "minecraft:stronghold=300",
        "minecraft:monument=301",
        "minecraft:trail_ruins=302"
    ));

    @Comment("Structures a Legendary Ruin Map may target, as structure_id=icon_id. Legendary icon ids are 400-415, omit =icon_id to use 400")
    @NestedType(String.class)
    public List<String> legendaryStructures = new ArrayList<>(Arrays.asList(
        "minecraft:mansion=400",
        "minecraft:ancient_city=401"
    ));

}
