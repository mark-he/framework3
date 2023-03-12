package com.eagletsoft.boot.framework.data.filter;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.common.utils.ApplicationUtils;
import com.eagletsoft.boot.framework.common.utils.BeanUtils;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.eagletsoft.boot.framework.common.utils.UnderlineToCamelUtils;
import com.eagletsoft.boot.framework.data.filter.impl.DefaultIndexFinder;
import com.eagletsoft.boot.framework.data.json.ExtViewHelper;
import com.eagletsoft.boot.framework.data.json.ExtViewportManager;
import com.eagletsoft.boot.framework.data.json.ExtendAnnotation;
import com.eagletsoft.boot.framework.data.json.meta.Many;
import com.eagletsoft.boot.framework.data.json.meta.One;
import com.fasterxml.jackson.databind.JavaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

public class NativeQueryBuilder {
    protected IndexFinder indexFinder = new DefaultIndexFinder();

    protected Map<String, Class> aliasMap = new HashMap<>();
    protected Map<String, Object> variables = new HashMap<>();
    protected String selectForm = "";
    protected String where = "";
    protected String orderBy = "";
    protected String aggregateWith = "";
    protected int from = 0;
    protected int size = 0;
    protected Class transform;
    protected int nameIdx;
    protected int maxSizeReturned;
    protected SimpleQuery query;

    protected NativeQueryBuilder(){}
    protected NativeQueryBuilder(SimpleQuery query){
        this.query = query;
    }

    public NativeQueryBuilder setIndexFinder(IndexFinder indexFinder) {
        this.indexFinder = indexFinder;
        return this;
    }

    public static NativeQueryBuilder newInstance() {
        return new NativeQueryBuilder();
    }

    public static NativeQueryBuilder newInstance(SimpleQuery query) {
        return new NativeQueryBuilder(query);
    }

    protected SimpleQuery getQuery() {
        if (null == query) {
            query =  ApplicationUtils.getBean(SimpleQuery.class);
        }
        return query;
    }

    public String printSQL() {
        return String.format("%s %s %s %s",
                selectForm,
                StringUtils.isEmpty(where) ? "" : " WHERE " + where,
                StringUtils.isEmpty(aggregateWith) ? "" : aggregateWith,
                StringUtils.isEmpty(orderBy) ? "" : " ORDER BY " + orderBy);
    }

    public Collection build() {
        return build(transform);
    }

    public <T> Collection<T> build(Class<T> transform) {
        SimpleQuery simpleQuery = getQuery();
        if (size > 0) {
            return simpleQuery.query(printSQL(), variables, from, size, true, transform);
        }
        else {
            return simpleQuery.query(printSQL(), variables, true, transform);
        }
    }

    public Object buildOne() {
        return buildOne(transform);
    }

    public <T> T buildOne(Class<T> transform) {
        SimpleQuery simpleQuery = getQuery();
        return simpleQuery.queryOne(printSQL(), variables, true, transform);
    }

    public PageResult buildPage() {
        return buildPage(transform);
    }

    public <T> PageResult<T> buildPage(Class<T> transform) {
        SimpleQuery simpleQuery = getQuery();
        return simpleQuery.queryPage(printSQL(), variables, from / size, size, true, transform);
    }

    public NativeQueryBuilder putVariables(Map variables) {
        this.variables.putAll(variables);
        return this;
    }

    public NativeQueryBuilder putVariable(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    public NativeQueryBuilder putAlias(String name, Class clazz) {
        aliasMap.put(name, clazz);
        return this;
    }

    public NativeQueryBuilder setAggregateWith(String aggregateWith) {
        this.aggregateWith = aggregateWith;
        return this;
    }

    public void setMaxSizeReturned(int maxSizeReturned) {
        this.maxSizeReturned = maxSizeReturned;
    }

    public NativeQueryBuilder selectForm(String sql) {
        this.selectForm = sql;
        return this;
    }

    public NativeQueryBuilder transform(Class clazz) {
        this.transform = clazz;
        return this;
    }


    public Map<String, Object> getVariables() {
        return variables;
    }

    public NativeQueryBuilder setPageNumber(int page, int size) {
        if (maxSizeReturned > 0) {
            size = Math.min(size, maxSizeReturned);
        }
        this.from = page * size;
        this.size = size;

        return this;
    }

    public NativeQueryBuilder setPage(int from, int size) {
        if (maxSizeReturned > 0) {
            size = Math.min(size, maxSizeReturned);
        }
        this.from = from;
        this.size = size;

        return this;
    }

    public NativeQueryBuilder where(String sql) {
        if (StringUtils.isNotEmpty(sql.trim())) {
            if (StringUtils.isNotEmpty(where)) {
                where += " AND ";
            }
            where += "(" + sql + ")";
        }
        return this;
    }

    public NativeQueryBuilder where(String sql, int from, int size) {
        return this.where(sql).setPage(from, size);
    }

    public NativeQueryBuilder where(PageSearch pageSearch) {
        if (!StringUtils.isEmpty(pageSearch.getSort())) {
            orderBy = makeNativeOrderBy(pageSearch.getSort());
        }
        return this.where(toWhereSQL(pageSearch)).setPage(pageSearch.getPage() * pageSearch.getSize(), pageSearch.getSize());
    }

    public NativeQueryBuilder orderBy(String sql) {
        this.orderBy = sql;
        return this;
    }

    private String toWhereSQL(PageSearch pageSearch) {
        return this.processFilters(pageSearch.getFilters(), ValueFilter.OP_AND);
    }

    private ExtViewAnn findExtViewAnn(Class root, String alias) {
        ExtViewAnn ret = new ExtViewAnn();
        ret.alias = alias;

        Collection<ExtendAnnotation> annotations = ExtViewportManager.getInstance().findExtendAnnotations(root);
        for (ExtendAnnotation ann: annotations) {
            if (ann.getMapping() instanceof One) {
                One one = (One)ann.getMapping();

                if (one.value().equals(alias)) {
                    ret.clazz = one.target();
                    ret.ref = one.ref();
                    ret.src = one.src();
                    if (StringUtils.isEmpty(ret.src)) {
                        ret.src = ann.getName();
                    }
                    break;
                }
            } else if (ann.getMapping() instanceof Many) {
                Many one = (Many)ann.getMapping();
                if (one.value().equals(alias)) {
                    ret.clazz = one.target();
                    ret.ref = one.ref();
                    ret.src = one.src();
                    if (StringUtils.isEmpty(ret.src)) {
                        ret.src = ann.getName();
                    }
                    break;
                }
            }
        }

        if (null != ret.clazz) {
            return ret;
        }
        return null;
    }

    private String exists(ExtViewAnn ann, ValueFilter c) {
        String cond = this.processFilter(ann.clazz, c);

        String bundleSQL = String.format("EXISTS (SELECT 1 FROM %s as %s WHERE %s = %s AND %s) "
                , UnderlineToCamelUtils.camelToUnderline(ann.clazz.getSimpleName())
                , ann.alias
                , ann.alias + "." + UnderlineToCamelUtils.camelToUnderline(ann.ref)
                , "a." + UnderlineToCamelUtils.camelToUnderline(ann.src)
                , cond
        );

        return bundleSQL;
    }

    private String processFilters(List<ValueFilter> cs, String andOr) {
        String bundleSQL = "";
        for (ValueFilter c : cs) {
            String cond = this.processFilter(c);
            if (StringUtils.isNotEmpty(cond)) {
                if (bundleSQL.length() > 0) {
                    bundleSQL = bundleSQL + " " + andOr + " ";
                }
                bundleSQL += cond;
            }
        }
        return bundleSQL;
    }

    private String processFilter(Class clazz, ValueFilter c) {
        String fieldName = c.getName();
        String op = c.getOp();
        Object value = c.getValue();

        String[] names = fieldName.split("\\.", -1);
        String property = names[names.length - 1];

        if (null != clazz) {//没有具体的类结构
            if (!indexFinder.isIndex(clazz, property)) {
                throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), fieldName + " is not searchable.");
            }

            PropertyDescriptor pd = null;

            try {
                pd = new PropertyDescriptor(property, clazz);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            Class pc = pd.getPropertyType();

            if (!ValueFilter.OP_IS_NULL.equals(op)) {
                if (pc.equals(Date.class)) {
                    value = new Date(Long.valueOf(value.toString()));
                }
            }
        }

        String condition = null;
        String nativeFieldName = null;
        if (names.length == 1) {
            nativeFieldName = "a." +  UnderlineToCamelUtils.camelToUnderline(names[0]);
        } else {
            nativeFieldName = names[0] + "." + UnderlineToCamelUtils.camelToUnderline(names[1]);
        }

        String var = fieldName + "_v"+ nameIdx;
        nameIdx ++;
        if (StringUtils.isEmpty(op) || ValueFilter.OP_EQ.equals(op)) {
            condition = String.format("%s = :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_NEQ.equals(op)) {
            condition = String.format("%s <> :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_GT.equals(op)) {
            condition = String.format("%s > :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_GE.equals(op)) {
            condition = String.format("%s >= :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_LT.equals(op)) {
            condition = String.format("%s < :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_LE.equals(op)) {
            condition = String.format("%s <= :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_LEFT_LIKE.equals(op)) {
            condition = String.format("%s like :%s", nativeFieldName, var);
            variables.put(var, makeLeftLike(value.toString()));
        } else if (ValueFilter.OP_LIKE.equals(op)) {
            condition = String.format("%s like :%s", nativeFieldName, var);
            variables.put(var,  makeLike(value.toString()));
        } else if (ValueFilter.OP_IN.equals(op)) {
            condition = String.format("%s in :%s", nativeFieldName, var);
            variables.put(var, value);
        } else if (ValueFilter.OP_NIN.equals(op)) {
            condition = String.format("NOT (%s in :%s)", nativeFieldName, var);
            variables.put(var, value);
        } else if(ValueFilter.OP_IS_NULL.equals(op)) {
            if("1".equals(value.toString()) || "true".equals(value.toString())) {
                condition = String.format("%s IS NULL", nativeFieldName);
            } else {
                condition = String.format("%s IS NOT NULL", nativeFieldName);
            }
        }

        return condition;
    }

    private String processFilter(ValueFilter c) {
        if (null == c.getValue()) {
            return null;
        }

        if (c.getValue() instanceof String && StringUtils.isEmpty(c.getValue().toString())) {
            return null;
        }

        if (ValueFilter.OP_OR.equals(c.getOp()) || ValueFilter.OP_AND.equals(c.getOp())) {
            JavaType type = JsonUtils.makeParametrizedClass(ArrayList.class, ValueFilter.class);
            List<ValueFilter> subFilters = JsonUtils.createMapper().convertValue(c.getValue(), type);
            String ret = processFilters(subFilters, c.getOp());
            return ret;
        }

        String[] names = c.getName().split("\\.", -1);
        String alias = null;
        if (names.length > 1) {
            alias = names[0];
            ExtViewAnn ann = this.findExtViewAnn(aliasMap.get("a"), alias);
            if (null != ann) {
                return this.exists(ann, c);
            }
        }

        if (null == alias) {
            alias = "a";
        }
        return this.processFilter(aliasMap.get("a"), c);
    }


    public static String makeLike(String in) {
        return "%" + in.replaceAll("%", "%%") + "%";
    }

    public static String makeLeftLike(String in) {
        return in.replaceAll("%", "%%") + "%";
    }

    public static void main(String[] args) {
        System.out.println(makeNativeOrderBy(" testA "));
        System.out.println(makeNativeOrderBy(" testA, test2 "));
        System.out.println(makeNativeOrderBy(" testA ASC"));
        System.out.println(makeNativeOrderBy(" testA, test2 DESC"));

    }

    public static String makeNativeOrderBy(String orderBy) {
        orderBy = orderBy.trim();

        String[] obs = orderBy.split("\\.", -1);
        if (obs.length == 1) {
            orderBy = "a." + obs[0];
        }

        int idx = orderBy.lastIndexOf(' ');
        if (idx > -1) {
            String sort = orderBy.substring(idx + 1).toUpperCase();
            if (sort.equals("DESC") || sort.equals("ASC")) {
                return UnderlineToCamelUtils.camelToUnderline(orderBy.substring(0, idx)) + " " + sort;
            }
        }
        return UnderlineToCamelUtils.camelToUnderline(orderBy);
    }

    private static class ExtViewAnn {
        String alias;
        Class clazz;
        String ref;
        String src;
    }
}
