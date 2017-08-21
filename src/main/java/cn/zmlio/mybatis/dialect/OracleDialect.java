package cn.zmlio.mybatis.dialect;

import cn.zmlio.mybatis.Dialect;

public class OracleDialect extends Dialect {

    @Override
    public String buildPageSQL(String originSQL, int offset, int limit) {
        String sql = "SELECT * FROM   \n" +
                "(  \n" +
                "SELECT A.*, ROWNUM RN   \n" +
                "FROM (" + originSQL + ") A   \n" +
                "WHERE ROWNUM <= " + (offset + limit) + "  \n" +
                ")  \n" +
                "WHERE RN >= " + offset;
        return sql;
    }
}
