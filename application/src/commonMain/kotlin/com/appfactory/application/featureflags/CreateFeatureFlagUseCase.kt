package com.appfactory.application.featureflags

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.model.FeatureFlag
import com.appfactory.domain.port.FeatureFlagRepository

import com.appfactory.domain.model.TeamId

class CreateFeatureFlagUseCase(
    private val repository: FeatureFlagRepository,
) {
    suspend operator fun invoke(
        teamId: TeamId,
        key: String,
        description: String,
        defaultEnabled: Boolean = false,
    ): DomainResult<FeatureFlag> {
        return when (val created = FeatureFlag.create(teamId, key, description, defaultEnabled)) {
            is DomainResult.Success -> repository.save(teamId, created.value)
            is DomainResult.Failure -> created
        }
    }
}
