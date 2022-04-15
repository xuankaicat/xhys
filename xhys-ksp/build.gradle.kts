plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    implementation(project(":xhys-core"))
    implementation(project(":xhys-ksp-annotation"))

    compileOnly("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

    // https://mvnrepository.com/artifact/com.squareup/kotlinpoet
    implementation("com.squareup:kotlinpoet:1.11.0")

    compileOnly(kotlin("compiler-embeddable"))
    // https://mvnrepository.com/artifact/com.google.devtools.ksp/symbol-processing-api
    compileOnly("com.google.devtools.ksp:symbol-processing-api:1.6.20-1.0.4")
}

buildConfig {
    packageName("$group.${project.name.split("-")[1]}")
    buildConfigField("String", "PACKAGE_NAME", "\"$group.ksp.generated\"")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
