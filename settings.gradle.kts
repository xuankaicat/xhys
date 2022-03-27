pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

rootProject.name = "xhys"
include("xhys-app")
include("xhys-core")
include("xhys-ksp")
