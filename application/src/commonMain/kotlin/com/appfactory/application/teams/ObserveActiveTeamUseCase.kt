package com.appfactory.application.teams

import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.ActiveTeamRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveTeamUseCase(
    private val activeTeamRepository: ActiveTeamRepository
) {
    operator fun invoke(): Flow<TeamId?> {
        return activeTeamRepository.observeActiveTeam()
    }
}
