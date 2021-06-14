package app.xuankai.xhys.mysql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

object DataMysql {
    private const val url = "jdbc:mysql://127.0.0.1:3306/xhys?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&tinyInt1isBit=false"
    private const val username = "root"
    private const val password = "280814"

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

    /**
     * 运行sql语句
     * @param sql String
     */
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

    /**
     * 获取一个或一组数据库对象的值，如果不存在则返回空集合
     * @param sql String
     * @return ArrayList<T>
     */
    inline fun <reified T : IObjectMysql> query(sql: String) : ArrayList<T>{
        val conn = openConnection()
        val stmt = conn?.createStatement()
        val list : ArrayList<T> = ArrayList()
        try{
            val resultSet = stmt!!.executeQuery(sql)
            val rsmd = resultSet.metaData
            val columns = rsmd.columnCount
            while(resultSet.next()){
                val obj : T = T::class.java.getConstructor().newInstance()
                for (i in 1..columns) {
                    obj.add(rsmd.getColumnName(i),
                            resultSet.getObject(i))
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

    /**
     * 获取一个值，如果表中不存在该数据则返回null
     * @param sql String
     * @return T?
     */
    inline fun <reified T : Any> getValue(sql: String) : T? {
        val conn = openConnection()
        val stmt = conn?.createStatement()
        try{
            val resultSet : ResultSet = stmt!!.executeQuery(sql)
            if(resultSet.next()){
                val res = resultSet.getObject(1) as? T
                resultSet.close()
                return res
            }else{
                resultSet.close()
                return null
            }
        }catch (e:Exception){
            e.printStackTrace()
            println("Mysql查询失败,语句为${sql}")
        }
        closeConnection(conn, stmt)
        return null
    }
}