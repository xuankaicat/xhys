plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":xhys-ksp-annotation"))

    ksp("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

    // https://mvnrepository.com/artifact/com.squareup/kotlinpoet
    implementation("com.squareup:kotlinpoet:1.10.2")

    compileOnly(kotlin("compiler-embeddable"))
    // https://mvnrepository.com/artifact/com.google.devtools.ksp/symbol-processing-api
    compileOnly("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.4")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}