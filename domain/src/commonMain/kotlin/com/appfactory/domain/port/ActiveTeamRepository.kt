package com.appfactory.domain.port

import com.appfactory.domain.model.TeamId
import kotlinx.coroutines.flow.Flow

interface ActiveTeamRepository {
    fun setActiveTeam(teamId: TeamId)
    fun observeActiveTeam(): Flow<TeamId?>
    suspend fun getActiveTeam(): TeamId?
}
