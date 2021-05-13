import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java

    val kotlinVersion = "1.4.32"
    kotlin("jvm") version kotlinVersion

    id("com.github.johnrengelman.shadow") version "6.1.0"//使用shadow对依赖进行打包
}

group = "app.xuankai"
version = "202105r5"

repositories {
    //maven {setUrl("http://maven.aliyun.com/nexus/content/groups/public/")}
    //maven {setUrl("https://dl.bintray.com/kotlin/kotlin-eap")}
    mavenCentral()
    jcenter()
}

tasks.withType(KotlinJvmCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    val miraiVersion = "2.6.4"

    // 开发时使用 mirai-core-api，运行时提供 mirai-core
    api("net.mamoe", "mirai-core-api", miraiVersion)
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion)

    //mysql
    implementation("org.springframework.boot:spring-boot-starter-jdbc:2.4.0")
    implementation("mysql:mysql-connector-java:8.0.23")
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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        useIR = true
    }
}