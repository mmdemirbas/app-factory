package com.appfactory.domain.common

/**
 * Domain-level result type.
 *
 * Use this in domain and application code instead of kotlin.Result
 * so that success/failure semantics are explicit and exhaustive.
 */
sealed class DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value

    fun <R> map(transform: (T) -> R): DomainResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    companion object {
        fun <T> success(value: T): DomainResult<T> = Success(value)
        fun failure(error: DomainError): DomainResult<Nothing> = Failure(error)
    }
}

sealed class DomainError(open val message: String) {
    data class NotFound(val id: EntityId) : DomainError("Entity not found: $id")
    data class ValidationFailed(override val message: String) : DomainError(message)
    data class Unauthorized(override val message: String = "Unauthorized") : DomainError(message)
    data class ExternalServiceError(override val message: String) : DomainError(message)
    data class Unknown(override val message: String) : DomainError(message)
}

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) action(value)
    return this
}

inline fun <T> DomainResult<T>.onFailure(action: (DomainError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Failure) action(error)
    return this
}
