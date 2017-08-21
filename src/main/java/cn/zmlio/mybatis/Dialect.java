package cn.zmlio.mybatis;

public abstract class Dialect {

    public String buildCountSQL(String originSQL) {
        String countSQL = "SELECT count(1) from (" + originSQL + ") as a";
        return countSQL;
    }

    public abstract String buildPageSQL(String originSQL, int offset, int limit);

    public boolean supportPaging() {
        return true;
    }

}
