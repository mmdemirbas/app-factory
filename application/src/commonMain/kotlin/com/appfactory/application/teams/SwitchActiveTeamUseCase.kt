package com.appfactory.application.teams

import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.ActiveTeamRepository

class SwitchActiveTeamUseCase(
    private val activeTeamRepository: ActiveTeamRepository
) {
    operator fun invoke(teamId: TeamId) {
        activeTeamRepository.setActiveTeam(teamId)
    }
}
