package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.IObjectMysql

class MyInt : IObjectMysql{
    var value: Int = 0

    override fun add(varName: String, value: Any?) {
        this.value = value as Int
    }

    companion object {
        fun ArrayList<MyInt>.toIntList(): List<Int> {
            return this.map { it.value }
        }
    }
}