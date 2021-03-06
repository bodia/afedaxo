package com.afedaxo.data.room

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithFiles(
    @Embedded var session: QuessingSession,
    @Relation(parentColumn = "sessionId", entity = DishEntity::class, entityColumn = "sessionId")
    var files: List<DishEntity>?
)