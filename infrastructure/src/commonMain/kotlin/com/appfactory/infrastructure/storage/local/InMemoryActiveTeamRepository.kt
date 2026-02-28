package com.appfactory.infrastructure.storage.local

import com.appfactory.domain.model.TeamId
import com.appfactory.domain.port.ActiveTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryActiveTeamRepository : ActiveTeamRepository {
    private val activeTeamFlow = MutableStateFlow<TeamId?>(com.appfactory.domain.common.EntityId("default_team"))

    override fun setActiveTeam(teamId: TeamId) {
        activeTeamFlow.value = teamId
    }

    override fun observeActiveTeam(): Flow<TeamId?> {
        return activeTeamFlow.asStateFlow()
    }

    override suspend fun getActiveTeam(): TeamId? {
        return activeTeamFlow.value
    }
}
