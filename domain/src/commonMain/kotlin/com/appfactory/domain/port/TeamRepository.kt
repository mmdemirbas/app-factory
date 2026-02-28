package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.TeamId
import com.appfactory.domain.model.UserId

/**
 * Team access repository used by backend authorization checks.
 */
interface TeamRepository {
    suspend fun isMember(teamId: TeamId, userId: UserId): DomainResult<Boolean>
}
