package app.xuankai.xhys.mysql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement


object DataMysql {
    val url = "jdbc:mysql://127.0.0.1:3306/xhys?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&tinyInt1isBit=false"
    val username = "root"
    val password = "280814"

    init{
        try{
            //Class.forName("com.mysql.cj.jdbc.Driver")
        }catch(e:Exception) {
            e.printStackTrace()
            println("Mysql连接失败")
        }
    }

    fun openConnection() :Connection?{
        return try {
            DriverManager.getConnection(url, username, password)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Mysql连接失败")
            null
        }
    }

    fun closeConnection(conn : Connection?, stmt : Statement?){
        conn?.close()
        stmt?.close()
    }

    fun executeSql(sql : String){
        val conn = openConnection()
        val stmt = conn?.createStatement()
        try{
            stmt!!.execute(sql)
        }catch (e:Exception) {
            e.printStackTrace()
            println("Mysql操作失败,语句为${sql}")
        }
        closeConnection(conn, stmt)
    }

    inline fun <reified T : IObjectMysql> query(sql: String) : ArrayList<T>{
        val conn = openConnection()
        val stmt = conn?.createStatement()
        val list : ArrayList<T> = ArrayList()
        try{
            val resultSet : ResultSet = stmt!!.executeQuery(sql)
            val rsmd = resultSet.metaData
            val columns = rsmd.columnCount
            while(resultSet.next()){
                val obj : T = T::class.java.getConstructor().newInstance()
                for (i in 0 until columns) {
                    obj.add(rsmd.getColumnName(i + 1),
                            resultSet.getObject(i + 1))
                }
                list.add(obj)
            }
            resultSet.close()
        }catch (e:Exception){
            e.printStackTrace()
            println("Mysql查询失败,语句为${sql}")
        }
        closeConnection(conn, stmt)
        return list
    }
}