plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("com.google.devtools.ksp") version "1.6.10-1.0.4" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
    }

    group = "com.github.xuankaicat.xhys"
    version = "2022m3r1"
}