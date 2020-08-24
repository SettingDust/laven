package me.settingdust.laven

import java.util.*

/**
 * @author pie-flavor
 */

/**
 * Converts a nullable into an optional via [Optional.ofNullable].
 */
fun <T : Any> T?.optional(): Optional<T> = Optional.ofNullable(this)

/**
 * Converts an optional into a nullable via [`orElse(null)`][Optional.orElse].
 */
fun <T> Optional<T>?.unwrap(): T? = this?.orElse(null)

/**
 * Converts an optional into a nullable via [`orElse(null)`][Optional.orElse].
 */
fun Optional<String>.unwrap(): String? = if (unwrap<String>().isNullOrBlank()) null else get()

val <T> Optional<T>.present
    get() = isPresent