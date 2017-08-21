package cn.zmlio.mybatis;

import cn.zmlio.mybatis.dialect.MySQLDialect;

public class DialectFactory {
    public static Dialect buildDialect(String dialectName) {
        dialectName = dialectName.toLowerCase();
        if (dialectName.equals("mysql")) {
            return new MySQLDialect();
        } else {
            return null;
        }
    }
}
