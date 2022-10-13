package com.tyrano.smsgames.light

data class LightGamemode(

    val id: Long,

    val name: String,

    val enabled: Boolean
) {
    override fun toString(): String {
        return "LightGamemode(id=$id, name='$name', enabled=$enabled)"
    }
}