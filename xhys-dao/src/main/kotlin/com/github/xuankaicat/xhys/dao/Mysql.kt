package com.github.xuankaicat.xhys.dao

import com.alibaba.druid.pool.DruidDataSourceFactory
import org.jetbrains.exposed.sql.Database
import java.util.*

object Mysql {
    val database: Database

    init {
        val properties = Properties()
        val stream = this::class.java.getResourceAsStream("/druid.properties")
        properties.load(stream)
        val dataSource = DruidDataSourceFactory.createDataSource(properties)

        database = Database.connect(dataSource)
    }
}
