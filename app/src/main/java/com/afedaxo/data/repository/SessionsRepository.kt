package com.afedaxo.data.repository

import androidx.lifecycle.LiveData
import com.afedaxo.data.room.AppDatabase
import com.afedaxo.data.room.DishEntity
import com.afedaxo.data.room.QuessingSession
import com.afedaxo.data.room.SessionWithFiles

class SessionsRepository (val appDatabase: AppDatabase, val filesRepository: FilesRepository) {
    suspend fun retrieveLastSession(): QuessingSession? {
        return appDatabase.quessingSessionDao.getLastSession()
    }

    fun getAllDishesForLastSessionLiveData() : LiveData<List<DishEntity>> {
        return appDatabase.quessingSessionDao.getAllDishesForLastSessionLiveData()
    }

    suspend fun deleteSession(quessingSession: QuessingSession?) {
        if (quessingSession != null) {
            deleteSession(
                appDatabase.quessingSessionDao.getSessionWithFilesBy(quessingSession.sessionId)
            )
        }
    }

    suspend fun deleteSession(sessionWithFiles: SessionWithFiles?) {
        if (sessionWithFiles != null) {
            appDatabase.quessingSessionDao.delete(sessionWithFiles.session)
            for (item in sessionWithFiles.files!!) {
                filesRepository.deleteIfExists(item.fullFilename)
                filesRepository.deleteIfExists(item.croppedFilename)
                appDatabase.quessingSessionDao.delete(item)
            }
        }
    }

    suspend fun insert(quessingSession: QuessingSession) {
        appDatabase.quessingSessionDao.insert(quessingSession)
    }

    suspend fun insert(dish: DishEntity) {
        appDatabase.quessingSessionDao.insert(dish)
    }

    suspend fun getAllDishesForSessionId(sessionId: Int): List<DishEntity> = appDatabase.quessingSessionDao.getAllDishesById(sessionId)
}