plugins {
    kotlin("jvm")
}

dependencies {
    api(platform("net.mamoe:mirai-bom:2.10.1"))
    api("net.mamoe:mirai-core-api")     // 编译代码使用
    runtimeOnly("net.mamoe:mirai-core") // 运行时使用

    implementation("mysql:mysql-connector-java:8.0.28")
}