package com.example.tvremote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tvremote.domain.model.MacroStep
import com.example.tvremote.domain.model.RemoteMacro
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "custom_macros")
data class MacroEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val stepsJson: String,
    val isPreset: Boolean
) {
    fun toDomain(): RemoteMacro {
        val steps = mutableListOf<MacroStep>()
        try {
            val jsonArray = JSONArray(stepsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                steps.add(
                    MacroStep(
                        commandKey = obj.getString("commandKey"),
                        delayMs = obj.optLong("delayMs", 400L),
                        extraArg = if (obj.has("extraArg") && !obj.isNull("extraArg")) obj.getString("extraArg") else null
                    )
                )
            }
        } catch (_: Exception) {}

        return RemoteMacro(
            id = id,
            name = name,
            description = description,
            iconName = iconName,
            steps = steps,
            isPreset = isPreset
        )
    }

    companion object {
        fun fromDomain(macro: RemoteMacro): MacroEntity {
            val jsonArray = JSONArray()
            macro.steps.forEach { step ->
                val obj = JSONObject()
                obj.put("commandKey", step.commandKey)
                obj.put("delayMs", step.delayMs)
                if (step.extraArg != null) obj.put("extraArg", step.extraArg)
                jsonArray.put(obj)
            }
            return MacroEntity(
                id = macro.id,
                name = macro.name,
                description = macro.description,
                iconName = macro.iconName,
                stepsJson = jsonArray.toString(),
                isPreset = macro.isPreset
            )
        }
    }
}
