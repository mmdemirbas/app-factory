package com.appfactory.application.teams

import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.ActiveTeamRepository

class GetActiveTeamUseCase(
    private val activeTeamRepository: ActiveTeamRepository
) {
    suspend operator fun invoke(): TeamId? {
        return activeTeamRepository.getActiveTeam()
    }
}
