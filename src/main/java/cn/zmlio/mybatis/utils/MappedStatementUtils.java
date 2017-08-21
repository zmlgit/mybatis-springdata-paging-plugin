package cn.zmlio.mybatis.utils;

import cn.zmlio.mybatis.Dialect;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class MappedStatementUtils {

    private static String COUNT_SUFFIX = "_Count";

    private static Dialect dialect;

    public static void setCountSuffix(String countSuffix) {
        MappedStatementUtils.COUNT_SUFFIX = countSuffix;
    }

    public static void setDialect(Dialect dialect) {
        MappedStatementUtils.dialect = dialect;
    }

    public static MappedStatement buildCountMappedStatement(final MappedStatement statement, final Object parameter) {
        Assert.notNull(MappedStatementUtils.dialect, "The sql dialect can't be null");

        SqlSource sqlSource = new StaticSqlSource(statement.getConfiguration(),
                dialect.buildCountSQL(statement.getBoundSql(parameter).getSql()),
                statement.getBoundSql(parameter).getParameterMappings());

        MappedStatement.Builder builder = new MappedStatement.Builder(statement.getConfiguration(),
                statement.getId() + COUNT_SUFFIX,
                sqlSource,
                statement.getSqlCommandType());

        builder.resource(statement.getResource());
        builder.fetchSize(statement.getFetchSize());
        builder.statementType(statement.getStatementType());
        builder.keyGenerator(statement.getKeyGenerator());
        if (statement.getKeyProperties() != null && statement.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : statement.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(statement.getTimeout());
        builder.parameterMap(statement.getParameterMap());
        //count查询返回值long
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap resultMap = new ResultMap.Builder(statement.getConfiguration(), statement.getId(), Long.class, new ArrayList<ResultMapping>()).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(statement.getResultSetType());
        builder.cache(statement.getCache());
        builder.flushCacheRequired(statement.isFlushCacheRequired());
        builder.useCache(statement.isUseCache());
        return builder.build();
    }

    public static MappedStatement buildPagingMappedStatement(final MappedStatement statement, final Object parameter, final Pageable pageParam) {
        SqlSource sqlSource = new StaticSqlSource(statement.getConfiguration(),
                dialect.buildPageSQL(statement.getBoundSql(parameter).getSql(), pageParam.getOffset(), pageParam.getPageSize()),
                statement.getBoundSql(parameter).getParameterMappings());

        MappedStatement.Builder builder = new MappedStatement.Builder(statement.getConfiguration(),
                statement.getId(),
                sqlSource,
                statement.getSqlCommandType());

        builder.resource(statement.getResource());
        builder.fetchSize(statement.getFetchSize());
        builder.statementType(statement.getStatementType());
        builder.keyGenerator(statement.getKeyGenerator());
        if (statement.getKeyProperties() != null && statement.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : statement.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(statement.getTimeout());
        builder.parameterMap(statement.getParameterMap());
        builder.resultMaps(statement.getResultMaps());
        builder.resultSetType(statement.getResultSetType());
        builder.cache(statement.getCache());
        builder.flushCacheRequired(statement.isFlushCacheRequired());
        builder.useCache(statement.isUseCache());

        return builder.build();

    }
}
