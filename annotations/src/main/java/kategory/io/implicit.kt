package kategory.io

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Retention(RUNTIME)
@Target(VALUE_PARAMETER, PROPERTY, FUNCTION)
annotation class implicit
