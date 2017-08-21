package cn.zmlio.mybatis;

import cn.zmlio.mybatis.utils.MappedStatementUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.zmlio.mybatis.utils.MappedStatementUtils.buildCountMappedStatement;
import static cn.zmlio.mybatis.utils.MappedStatementUtils.buildPagingMappedStatement;

@Intercepts({
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class MyBatisSpringDataPagingInterceptor implements Interceptor {

    private final static int INDEX_MAPPED_STATEMENT = 0;
    private final static int INDEX_PARAMETER = 1;
    private final static int INDEX_ROW_BOUNDS = 2;
    private final static int INDEX_RESULT_HANDLER = 3;
    private final static String COUNT_SUFFIX = "_Count";
    private final static String PAGING_SUFFIX = "_Paging";
    private final Map<String, Boolean> mappedMethods = new ConcurrentHashMap<String, Boolean>();
    private Dialect dialect;
    private String[] excludeQuery;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object[] args = invocation.getArgs();

        MappedStatement mappedStatement = (MappedStatement) args[INDEX_MAPPED_STATEMENT];
        Object parameter = args[INDEX_PARAMETER];

        String statementId = mappedStatement.getId();

        if (!dialect.supportPaging() || !isSpringDataPageQuery(statementId, parameter)) {
            return invocation.proceed();
        }

        for (String exclude : excludeQuery) {
            if (statementId.equals(exclude)) {
                return invocation.proceed();
            }
        }

        ResultHandler resultHandler = (ResultHandler) args[INDEX_RESULT_HANDLER];
        Executor executor = (Executor) invocation.getTarget();
        Pageable pageParam = findPageParam(parameter);

        List content = queryContent(executor, mappedStatement, resultHandler, parameter, pageParam);
        long count = queryCount(executor, mappedStatement, resultHandler, parameter);

        Page result = new PageImpl(content, pageParam, count);

        List<Page> pages = new ArrayList<Page>();
        pages.add(result);
        return pages;
    }

    private List queryContent(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, Object parameter, Pageable pageParam) throws SQLException {
        MappedStatement contentMappedStatement = buildPagingMappedStatement(mappedStatement, parameter, pageParam);
        RowBounds rowBounds = new RowBounds(pageParam.getOffset(), pageParam.getPageSize());
        List result = executor.query(contentMappedStatement, parameter, rowBounds, resultHandler);
        return result;
    }


    private long queryCount(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, Object parameter) throws SQLException {
        MappedStatement countMappedStatement = buildCountMappedStatement(mappedStatement, parameter);
        List<Long> result = executor.query(countMappedStatement, parameter, RowBounds.DEFAULT, resultHandler);
        return result.size() == 0 ? 0 : (result.get(0) == null ? 0 : result.get(0));
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);//使用Plugin的wrap方法生成代理对象
    }

    @Override
    public void setProperties(Properties props) {
        MappedStatementUtils.setCountSuffix(COUNT_SUFFIX);
        String dialectProp = props.getProperty("dialect", "mysql");
        String excluded = props.getProperty("exclude", "");
        dialect = DialectFactory.buildDialect(dialectProp);
        if (dialect == null) {
            throw new IllegalArgumentException("dialect not supported");
        }
        this.excludeQuery = excluded.split(",");
        MappedStatementUtils.setDialect(dialect);

    }

    private boolean isSpringDataPageQuery(String methodName, Object parameter) {
        if (this.mappedMethods.containsKey(methodName)) {
            return mappedMethods.get(methodName);
        } else {
            this.mappedMethods.put(methodName, findPageParam(parameter) != null);
            return mappedMethods.get(methodName);
        }
    }

    /**
     * 从当前的参数列表中获取pageable 参数，如果为空代表当前查询不是分页查询
     *
     * @param parameter // 当前的查询参数
     * @return
     */
    private Pageable findPageParam(Object parameter) {

        if (parameter instanceof Map) {// 除了[pageable] 之外还有其他参数的情况
            Map parameterMap = (Map) parameter;
            Pageable pageable = null;
            Iterator iterator = parameterMap.values().iterator();
            while (iterator.hasNext()) {
                Object val = iterator.next();
                if (val instanceof Pageable) {
                    pageable = (Pageable) val;
                    break;
                }
            }
            if (pageable != null) {
                return pageable;
            }
        } else {// 只有一个参数的情况
            return (Pageable) parameter;
        }
        return null;
    }

}
