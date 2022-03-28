package com.github.xuankaicat.xhys.ksp.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Command(vararg val name: String)
