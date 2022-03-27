plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    api(project(":xhys-core"))

    api(platform("net.mamoe:mirai-bom:2.10.1"))
    api("net.mamoe:mirai-core-api")     // 编译代码使用
    runtimeOnly("net.mamoe:mirai-core") // 运行时使用

    //mysql
    implementation("org.springframework.boot:spring-boot-starter-jdbc:2.6.5")
    implementation("mysql:mysql-connector-java:8.0.28")
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