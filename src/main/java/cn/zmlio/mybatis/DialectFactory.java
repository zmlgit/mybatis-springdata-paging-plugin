package cn.zmlio.mybatis;

import cn.zmlio.mybatis.dialect.MySQLDialect;
import cn.zmlio.mybatis.dialect.OracleDialect;

public class DialectFactory {
    public static Dialect buildDialect(String dialectName) {
        dialectName = dialectName.toLowerCase();
        if (dialectName.equals("mysql")) {
            return new MySQLDialect();
        } else if (dialectName.equals("oracle")) {
            return new OracleDialect();
        } else {
            return null;
        }
    }
}
