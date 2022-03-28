package com.github.xuankaicat.xhys.ksp.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Command(vararg val name: String)
