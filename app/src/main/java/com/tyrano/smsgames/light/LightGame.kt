package com.tyrano.smsgames.light

import java.time.Instant

data class LightGame(

    val id: Long,

    val gameName: String,

    val ownerName: String,

    val start: Instant?
) {
    override fun toString(): String {
        return "LightGame(id=$id, gameName='$gameName', ownerName='$ownerName', start=$start)"
    }
}