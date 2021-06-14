package app.xuankai.xhys.mysql

import java.sql.Connection

class ConnPoolProxy(private val conn: Connection): Connection by conn {
    override fun close() {
        ConnPool.connPool.add(conn)
    }
}