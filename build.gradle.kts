import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java

    val kotlinVersion = "1.5.31"
    kotlin("jvm") version kotlinVersion

    id("com.github.johnrengelman.shadow") version "6.1.0"//使用shadow对依赖进行打包
}

group = "app.xuankai"
version = "2021m10r2"

repositories {
    //maven {setUrl("http://maven.aliyun.com/nexus/content/groups/public/")}
    //maven {setUrl("https://dl.bintray.com/kotlin/kotlin-eap")}
    mavenCentral()
    jcenter()
}

dependencies {
    val miraiVersion = "2.8.0-M1"

    // 开发时使用 mirai-core-api，运行时提供 mirai-core
    api("net.mamoe", "mirai-core-api", miraiVersion)
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion)

    //mysql
    implementation("org.springframework.boot:spring-boot-starter-jdbc:2.5.5")
    implementation("mysql:mysql-connector-java:8.0.25")
}

tasks.withType<ShadowJar> {
    // 生成包的命名规则： baseName-version-classifier.jar
    manifest {
        attributes(
            Pair("Main-Class","app.xuankai.xhys.MyLoaderKt")
        )
    }

    // 将 build.gradle 打入到 jar 中, 方便查看依赖包版本
    from("./"){
        include("build.gradle.kts")
    }
}