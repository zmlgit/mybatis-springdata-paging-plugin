package cn.zmlio.mybatis.dialect;

import cn.zmlio.mybatis.Dialect;

public class MySQLDialect extends Dialect {
    @Override
    public String buildPageSQL(String originSQL, int offset, int limit) {
        String pageSQL = "SELECT * FROM (" + originSQL + ") AS B LIMIT " + offset + "," + limit;
        return pageSQL;
    }
}
