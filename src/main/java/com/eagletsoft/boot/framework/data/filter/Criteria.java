package com.eagletsoft.boot.framework.data.filter;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import com.eagletsoft.boot.framework.common.utils.BeanUtils;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import com.eagletsoft.boot.framework.data.filter.impl.DefaultIndexFinder;
import com.eagletsoft.boot.framework.data.json.ExtViewHelper;
import com.eagletsoft.boot.framework.data.json.ExtViewportManager;
import com.eagletsoft.boot.framework.data.json.ExtendAnnotation;
import com.eagletsoft.boot.framework.data.json.meta.Many;
import com.eagletsoft.boot.framework.data.json.meta.One;
import com.fasterxml.jackson.databind.JavaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

public class Criteria<T> implements Specification<T> {
	private static final Logger LOG = LoggerFactory.getLogger(Criteria.class);
	private PageSearch search;
	private CriteriaModifier<T>[] modifiers;
	private IndexFinder indexFinder = new DefaultIndexFinder();

	public Criteria(PageSearch search, IndexFinder indexFinder, CriteriaModifier<T>... modifiers) {
		this.search = search;
		this.modifiers = modifiers;
		this.indexFinder = indexFinder;
	}

	public Criteria(PageSearch search) {
		this.search = search;
	}
	
	public Criteria(PageSearch search, CriteriaModifier<T>... modifiers) {
		this.modifiers = modifiers;
		this.search = search;
	}
	
	private Comparable getComparable(Object value) {
		return (Comparable)value;
	}
	
	private Collection getCollection(Object value) {
		return (Collection)value;
	}
	
	public static Predicate concat(CriteriaBuilder cb, Predicate p, Predicate p2, boolean or) {
		if (null != p) {
			if (or) {
				return cb.or(p, p2);
			}
			else {
				return cb.and(p, p2);
			}
		}
		else  {
			return p2;
		}
	}

	private Predicate processFilters(Root<T> root, CriteriaBuilder cb, Iterable<ValueFilter> cs, boolean or) throws Exception {
		Predicate p = null;
		if (null != cs) {
			for (ValueFilter c : cs) {
				p = processFilter(root, cb, p, c, or);
			}
		}
		return p;
	}

	private Predicate processFilter(Root<T> root, CriteriaBuilder cb, Predicate p, ValueFilter c, boolean or) throws Exception {
		if (null == c.getValue()) {
			return p;
		}

		if (c.getValue() instanceof String && StringUtils.isEmpty(c.getValue().toString())) {
			return p;
		}

		if (c.getName().contains(".")) {
			p = this.join(root, cb, p, c, or);
			return p;
		}
		if (ValueFilter.OP_OR.equals(c.getOp()) || ValueFilter.OP_AND.equals(c.getOp())) {
			JavaType type = JsonUtils.makeParametrizedClass(ArrayList.class, ValueFilter.class);
			List<ValueFilter> subFilters = JsonUtils.createMapper().convertValue(c.getValue(), type);

			Predicate p2 = processFilters(root, cb,  subFilters, ValueFilter.OP_OR.equals(c.getOp()));
			return concat(cb, p, p2, or);
		}

		if (!indexFinder.isIndex(root.getJavaType(), c.getName())) {
			throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), c.getName() + " is not searchable.");
		}

		Object value = c.getValue();
		PropertyDescriptor pd = new PropertyDescriptor(c.getName(), root.getJavaType());

		Class pc = pd.getPropertyType();

		if (!ValueFilter.OP_IS_NULL.equals(c.getOp())) {
			if (pc.equals(Date.class)) {
				value = new Date(Long.valueOf(c.getValue().toString()));
			}
		}

		Path path = root.get(c.getName());
		if (StringUtils.isEmpty(c.getOp()) || ValueFilter.OP_EQ.equals(c.getOp())) {
			p = concat(cb, p, cb.equal(path, value), or);
		} else if (ValueFilter.OP_NEQ.equals(c.getOp())) {
			p = concat(cb, p, cb.notEqual(path, value), or);
		} else if (ValueFilter.OP_GT.equals(c.getOp())) {
			p = concat(cb, p, cb.greaterThan(path, getComparable(value)), or);
		} else if (ValueFilter.OP_GE.equals(c.getOp())) {
			p = concat(cb, p, cb.greaterThanOrEqualTo(path, getComparable(value)), or);
		} else if (ValueFilter.OP_LT.equals(c.getOp())) {
			p = concat(cb, p, cb.lessThan(path, getComparable(value)), or);
		} else if (ValueFilter.OP_LE.equals(c.getOp())) {
			p = concat(cb, p, cb.lessThanOrEqualTo(path, getComparable(value)), or);
		} else if (ValueFilter.OP_LEFT_LIKE.equals(c.getOp())) {
			p = concat(cb, p, cb.like(path, makeLeftLike(value.toString())), or);
		} else if (ValueFilter.OP_LIKE.equals(c.getOp())) {
			p = concat(cb, p, cb.like(path, makeLike(value.toString())), or);
		} else if (ValueFilter.OP_IN.equals(c.getOp())) {
			p = concat(cb, p, cb.in(path).value(getCollection(value)), or);
		} else if (ValueFilter.OP_NIN.equals(c.getOp())) {
			p = concat(cb, p, cb.or(cb.isNull(path), cb.not(cb.in(path).value(getCollection(value)))), or);
		} else if(ValueFilter.OP_IS_NULL.equals(c.getOp())) {
			if("1".equals(value.toString()) || "true".equals(value.toString())) {
				p = concat(cb, p, cb.isNull(path), or);
			} else {
				p = concat(cb, p, cb.isNotNull(path), or);
			}
		}

		return p;
	}

	private Predicate join(Root<T> root, CriteriaBuilder cb, Predicate p, ValueFilter c, boolean or) throws Exception {
		String[] args = c.getName().split("\\.");

		String object = args[0];
		String field = args[1];

		Class targetClass = null;
		String targetRef = null;
		String rootField = null;

		Collection<ExtendAnnotation> annotations = ExtViewportManager.getInstance().findExtendAnnotations(root.getJavaType());
		for (ExtendAnnotation ann: annotations) {
			if (ann.getMapping() instanceof One) {
				One one = (One)ann.getMapping();
				if (one.value().equals(object)) {
					targetClass = one.target();
					targetRef = one.ref();
					rootField = one.src();
					if (StringUtils.isEmpty(rootField)) {
						rootField = ann.getName();
					}
					break;
				}
			} else if (ann.getMapping() instanceof Many) {
				Many one = (Many)ann.getMapping();
				if (one.value().equals(object)) {
					targetClass = one.target();
					targetRef = one.ref();
					rootField = one.src();
					if (StringUtils.isEmpty(rootField)) {
						rootField = ann.getName();
					}
					break;
				}
			}
		}

		if (null == targetClass) {
			throw new ServiceException(StandardErrors.INTERNAL_ERROR.getStatus(), c.getName() + " is not join with @One or @Many.");
		}


		ValueFilter newFilter = c.clone();
		newFilter.setName(field);

		CriteriaQuery<?> criteriaQuery = cb.createQuery();
		Subquery subquery = criteriaQuery.subquery(Long.class);

		Root subRoot = subquery.from(targetClass);
		subquery.select(subRoot.get(targetRef));
		Path path = root.get(rootField);
		Predicate subP = cb.equal(path, subRoot.get(targetRef));
		subP = processFilter(subRoot, cb, subP, newFilter, false);

		subquery.where(subP);
		p = concat(cb, p, cb.exists(subquery), false);
		return p;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		try {
			Predicate p = processFilters(root, cb, search.getFilters(), false);
			if (null != this.modifiers) {
				for (CriteriaModifier<T> cm : this.modifiers) {
					if (null != cm) {
						Predicate p2 = cm.onSearch(root, cb, p);
						if (null != p2) {
							p = concat(cb, p, p2, false);
						}
					}
				}
			}
			
			if (null != p) {
				query.where(p);
			}
			if (!StringUtils.isEmpty(search.getSort())) {
				List<Order> list = new ArrayList<>();
				String[] sorts = search.getSort().split(",", -1);
				
				for (String sort : sorts) {
					String[] sa = sort.trim().split(" ", -1);
					if (sa.length > 1 && sa[1].equalsIgnoreCase("DESC")) {
						list.add(cb.desc(root.get(sa[0])));
					}
					else {
						list.add(cb.asc(root.get(sa[0])));
					}
				}
				query.orderBy(list);
			}
		} catch (ServiceException sx) {
			throw sx;
		} catch (Exception ex) {
			throw new ServiceException(ex);
		}
		return null;
	}
	
	
	private static String makeLike(String in) {
		return "%" + in.replaceAll("%", "%%") + "%";
	}

	private static String makeLeftLike(String in) {
		return in.replaceAll("%", "%%") + "%";
	}

	private static class ValueDesc<Y> {
		Path<Y> path;
		Object value;

		public ValueDesc(Path<Y> path, Object value) {
			super();
			this.path = path;
			this.value = value;
		}
	}
	
	public interface CriteriaModifier<T> {
		Predicate onSearch(Root<T> root, CriteriaBuilder cb, Predicate p);
	}
}
