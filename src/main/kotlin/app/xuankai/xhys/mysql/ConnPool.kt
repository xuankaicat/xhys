package app.xuankai.xhys.mysql

import kotlinx.coroutines.*
import okhttp3.internal.wait
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.logging.Logger
import javax.sql.DataSource

/**
 * 数据库连接池类
 */
class ConnPool : DataSource{

    companion object {
        private const val url = "jdbc:mysql://127.0.0.1:3306/xhys?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&tinyInt1isBit=false"
        private const val username = "root"
        private const val password = "280814"
        private const val poolSize = 10

        val connPool = LinkedList<Connection>()

        fun initConnPool() {
            connPool.clear()
            for(i in 0 until poolSize) {
                val conn = DriverManager.getConnection(url, username, password)
                connPool.add(conn)
            }
        }

        fun checkValid() {
            //每8小时强制刷新一次连接池中的所有连接以防止过期
            CoroutineScope(Dispatchers.Default).launch {
                while(true) {
                    delay(28800000L)
                    if(connPool.isNotEmpty()) {
                        for (conn in connPool) {
                            conn.close()
                        }
                        initConnPool()
                    }
                }
            }
        }
    }

    init {
        initConnPool()
        checkValid()
    }

    override fun getConnection(): Connection {
        while(connPool.size == 0) {
            try {
                this.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        val conn = connPool.removeFirst()
        return ConnPoolProxy(conn)
    }

    override fun getLogWriter(): PrintWriter? = null

    override fun setLogWriter(out: PrintWriter?) {}

    override fun setLoginTimeout(seconds: Int) {}

    override fun getLoginTimeout(): Int = 0

    override fun getParentLogger(): Logger? = null

    override fun <T : Any?> unwrap(iface: Class<T>?): T? = null

    override fun isWrapperFor(iface: Class<*>?): Boolean = false

    override fun getConnection(username: String?, password: String?): Connection? = null
}

/**
 * 数据库连接池中的连接的代理类
 * @property conn Connection 要代理的连接
 */
class ConnPoolProxy(private val conn: Connection): Connection by conn {
    override fun close() {
        ConnPool.connPool.add(conn)
    }
}