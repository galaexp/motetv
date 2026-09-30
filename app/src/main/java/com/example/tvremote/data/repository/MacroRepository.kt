package com.example.tvremote.data.repository

import com.example.tvremote.data.local.dao.MacroDao
import com.example.tvremote.data.local.entity.MacroEntity
import com.example.tvremote.domain.model.RemoteMacro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MacroRepository(private val macroDao: MacroDao) {
    val macros: Flow<List<RemoteMacro>> = macroDao.getAllMacros().map { list ->
        if (list.isEmpty()) {
            RemoteMacro.defaultPresets()
        } else {
            list.map { it.toDomain() }
        }
    }

    suspend fun saveMacro(macro: RemoteMacro) {
        macroDao.insertMacro(MacroEntity.fromDomain(macro))
    }

    suspend fun deleteMacro(macroId: String) {
        macroDao.deleteMacroById(macroId)
    }
}
