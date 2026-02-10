package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import kotlinx.serialization.Serializable

@Serializable
data class Cheat(
    val name: String,
    val code: String,
    var enabled: Boolean = true
)
