package com.economique.common.result

sealed class Result<out T, out E> {
    data class Ok<out T>(val value: T) : Result<T, Nothing>()
    data class Err<out E>(val error: E) : Result<Nothing, E>()

    val isOk: Boolean get() = this is Ok
    val isErr: Boolean get() = this is Err

    fun <R> map(transform: (T) -> R): Result<R, E> = when (this) {
        is Ok -> Ok(transform(value))
        is Err -> this
    }

    fun <F> mapError(transform: (E) -> F): Result<T, F> = when (this) {
        is Ok -> this
        is Err -> Err(transform(error))
    }

    fun <R> flatMap(transform: (T) -> Result<R, @UnsafeVariance E>): Result<R, E> = when (this) {
        is Ok -> transform(value)
        is Err -> this
    }

    fun <R> fold(onSuccess: (T) -> R, onFailure: (E) -> R): R = when (this) {
        is Ok -> onSuccess(value)
        is Err -> onFailure(error)
    }

    fun getOrNull(): T? = when (this) {
        is Ok -> value
        is Err -> null
    }

    fun getOrElse(default: (E) -> @UnsafeVariance T): T = when (this) {
        is Ok -> value
        is Err -> default(error)
    }

    fun onSuccess(action: (T) -> Unit): Result<T, E> {
        if (this is Ok) action(value)
        return this
    }

    fun onFailure(action: (E) -> Unit): Result<T, E> {
        if (this is Err) action(error)
        return this
    }

    companion object {
        fun <T> ok(value: T): Result<T, Nothing> = Ok(value)
        fun <E> err(error: E): Result<Nothing, E> = Err(error)
    }
}

fun <T, E> Result<T, E>.andThen(transform: (T) -> Result<T, E>): Result<T, E> = flatMap(transform)
