plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

dependencies {
    implementation(project(":xhys-core"))
    implementation(project(":xhys-ksp-annotation"))
    ksp(project(":xhys-ksp"))

    api(platform("net.mamoe:mirai-bom:2.10.1"))
    api("net.mamoe:mirai-core-api")     // 编译代码使用
    runtimeOnly("net.mamoe:mirai-core") // 运行时使用

    //mysql
    //implementation("org.springframework.boot:spring-boot-starter-jdbc:2.6.5")
    implementation("mysql:mysql-connector-java:8.0.28")
}

buildConfig {
    packageName(group.toString())
    buildConfigField("String", "VERSION_NAME", "\"$version\"")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    // 生成包的命名规则： baseName-version-classifier.jar
    manifest {
        attributes(
            Pair("Main-Class", "com.github.xuankaicat.xhys.MyLoaderKt")
        )
    }

    // 将 build.gradle 打入到 jar 中, 方便查看依赖包版本
    from("./"){
        include("build.gradle.kts")
    }
}