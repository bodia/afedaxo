package com.afedaxo.domain.usecase

import android.graphics.Bitmap
import com.afedaxo.data.repository.FilesRepository
import com.afedaxo.data.repository.SessionsRepository
import com.afedaxo.domain.Either
import com.afedaxo.domain.Failure
import com.afedaxo.domain.Success
import com.afedaxo.domain.model.CalculationParams
import com.afedaxo.processor.CompositeWeightProcessor
import timber.log.Timber

class CalcDishForPeopleUseCase(val filesRepository: FilesRepository,
                               val sessionsRepository: SessionsRepository): UseCase<List<Pair<Int, Bitmap>>, CalculationParams>() {

    override suspend fun run(param: CalculationParams): Either<Exception, List<Pair<Int, Bitmap>>> {
        return try {
            val dishes = sessionsRepository.getAllDishesForSessionId(param.sessionId)
            Timber.d("Got dishes for session %d, size: %d, price mode: %d",
                param.sessionId, dishes.size, param.priceMode)

            val preproc = CompositeWeightProcessor.preprocessValues(param.numPeople, dishes)

            val compositeWeightProcessor = CompositeWeightProcessor(preproc)

            Timber.d(compositeWeightProcessor.toString())

            if (param.priceMode == 0) {
                val resultingDish = compositeWeightProcessor.getWeightedRandom()
                if (resultingDish != null) {
                    Success(resultingDish.map {
                        Pair(it.first, filesRepository.getBitmapOfFile(it.second.croppedFilename))
                    })
                }
                else {
                    Failure(java.lang.Exception("CalcDishForPeopleUseCase error! Res dish null"))
                }
            } else if (param.priceMode == 1) {
                val fullyRandom = preproc.random()
                val weightedRandom = compositeWeightProcessor.getWeightedRandom()
                val resultingDish = listOf(fullyRandom, weightedRandom).random()
                if (resultingDish != null) {
                    Success(resultingDish.map {
                        Pair(it.first, filesRepository.getBitmapOfFile(it.second.croppedFilename))
                    })
                }
                else {
                    Failure(java.lang.Exception("CalcDishForPeopleUseCase error! Res dish null"))
                }
            } else {
                val fullyRandom = preproc.random()
                Success(fullyRandom.map {
                    Pair(it.first, filesRepository.getBitmapOfFile(it.second.croppedFilename))
                })
            }
        }
        catch (e: java.lang.Exception) {
            Failure(e)
        }
    }
}