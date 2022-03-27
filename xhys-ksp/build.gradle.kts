plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":xhys-core"))
    // https://mvnrepository.com/artifact/com.squareup/kotlinpoet
    implementation("com.squareup:kotlinpoet:1.10.2")

    compileOnly(kotlin("compiler-embeddable"))
    // https://mvnrepository.com/artifact/com.google.devtools.ksp/symbol-processing-api
    compileOnly("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.4")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}