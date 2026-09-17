package com.holybuckets.ruinmap;

import com.holybuckets.ruinmap.client.CommonClassClient;
import net.blay09.mods.balm.api.client.BalmClient;
import net.fabricmc.api.ClientModInitializer;


public class RuinMapMainFabricClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        BalmClient.initialize(Constants.MOD_ID, CommonClassClient::initClient);
    }

}
