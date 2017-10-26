package com.mrkirby153.playtime

import net.minecraftforge.fml.common.Mod

@Mod(modid = Constants.MODID, version = Constants.VERSION, name = Constants.NAME, acceptableRemoteVersions = "*")
class PlaytimeTracker {
    @Mod.Instance(Constants.MODID)
    lateinit var instance: PlaytimeTracker

    
}