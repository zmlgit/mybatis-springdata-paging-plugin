package cn.zmlio.mybatis;

public abstract class Dialect {

    public String buildCountSQL(String originSQL) {

        if (isUnionQuery(originSQL)) {
            return "SELECT count(1) from (" + originSQL + ") as a";
        } else {
            int selectIndex = originSQL.toLowerCase().indexOf("select");
            int fromIndex = originSQL.toLowerCase().indexOf("from");
            String countSQL = originSQL.substring(selectIndex, selectIndex + "select".length());
            countSQL += " count(1) ";
            countSQL += originSQL.substring(fromIndex);
            return countSQL;
        }
    }

    public abstract String buildPageSQL(String originSQL, int offset, int limit);

    protected boolean isUnionQuery(String originSql) {// 使用union的语句count无法优化
        return originSql.toLowerCase().contains("union");
    }

    public boolean supportPaging() {
        return true;
    }

}
