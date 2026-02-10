package com.noobzsociety.smsgames.data.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

data class EditableFlowSetting<T>(
    val flow: Flow<T>,
    private val getValue: () -> T,
    private val setValue: (T) -> Unit,
) {
    var value: T
        get() = getValue()
        set(value) = setValue(value)

//    fun update(update: (T) -> T) {
//        setValue(update(getValue()))
//    }
}

@OptIn(ExperimentalSettingsApi::class)
inline fun <reified T : Any> ObservableSettings.serializedFlowSetting(
    key: String,
    default: T,
): EditableFlowSetting<T> {
    val serializer = serializer<T>()
    val defaultValue = Json.encodeToString(serializer, default)

    return EditableFlowSetting(
        flow = getStringFlow(key, defaultValue).map { Json.decodeFromString(serializer, it) },
        getValue = { Json.decodeFromString(serializer, this.getString(key, defaultValue)) },
        setValue = { value -> putString(key, Json.encodeToString(serializer, value)) },
    )
}
