package com.appfactory.application.featureflags

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.model.Environment
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.port.FeatureFlagRepository

import com.appfactory.domain.model.TeamId

class ToggleFeatureFlagUseCase(
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(
        teamId: TeamId,
        flagId: EntityId,
        environment: Environment,
        enabled: Boolean,
    ): DomainResult<FeatureFlag> {
        val flag = repository.getById(teamId, flagId)
            ?: return DomainResult.failure(DomainError.NotFound(flagId))

        val updated = flag.copy(
            environmentOverrides = flag.environmentOverrides + (environment to enabled),
            updatedAt = Timestamp.now(),
        )
        return repository.save(teamId, updated)
    }
}
