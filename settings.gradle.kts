pluginManagement {
    plugins {
        kotlin("jvm") version "1.6.20"
        id("com.github.johnrengelman.shadow") version "7.1.2"
        id("com.google.devtools.ksp") version "1.6.20-1.0.4"
        id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

rootProject.name = "xhys"
include("xhys-app")
include("xhys-core")
include("xhys-ksp")
include("xhys-ksp-annotation")
include("xhys-dao")
