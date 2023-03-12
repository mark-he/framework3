package com.eagletsoft.boot.framework.data.json.modify;

import com.eagletsoft.boot.framework.data.json.ExtRetriever;
import com.eagletsoft.boot.framework.data.json.ExtViewportManager;
import com.eagletsoft.boot.framework.data.json.FormulaDedutor;
import com.eagletsoft.boot.framework.data.json.ViewportConfig;
import com.eagletsoft.boot.framework.data.json.context.ExtViewContext;
import com.eagletsoft.boot.framework.data.json.context.KeyValue;
import com.eagletsoft.boot.framework.data.json.meta.ExtView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class ExtBeanSerializer extends BeanSerializerBase
{
    private static final long serialVersionUID = -3618164443537292758L;

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> set) {
        return this;
    }

    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */

    /**
     * @param builder Builder object that contains collected information
     *   that may be needed for serializer
     * @param properties Property writers used for actual serialization
     */
    public ExtBeanSerializer(JavaType type, BeanSerializerBuilder builder,
            BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties)
    {
        super(type, builder, properties, filteredProperties);
    }
    
    /**
     * Alternate copy constructor that can be used to construct
     * standard {@link BeanSerializer} passing an instance of
     * "compatible enough" source serializer.
     */
    protected ExtBeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    protected ExtBeanSerializer(BeanSerializerBase src,
            ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
    }

    protected ExtBeanSerializer(BeanSerializerBase src,
            ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
    }
    
    protected ExtBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
        super(src, toIgnore);
    }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, fluent factories
    /**********************************************************
     */

    /**
     * Method for constructing dummy bean serializer; one that
     * never outputs any properties
     */
    public static BeanSerializer createDummy(JavaType forType)
    {
        return new BeanSerializer(forType, null, NO_PROPS, null);
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new UnwrappingBeanSerializer(this, unwrapper);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new ExtBeanSerializer(this, objectIdWriter, _propertyFilterId);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new ExtBeanSerializer(this, _objectIdWriter, filterId);
    }

    @Override
    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
        return new ExtBeanSerializer(this, toIgnore);
    }

    /**
     * Implementation has to check whether as-array serialization
     * is possible reliably; if (and only if) so, will construct
     * a {@link BeanAsArraySerializer}, otherwise will return this
     * serializer as is.
     */
    @Override
    protected BeanSerializerBase asArraySerializer()
    {
        /* Can not:
         * 
         * - have Object Id (may be allowed in future)
         * - have "any getter"
         * - have per-property filters
         */
        if ((_objectIdWriter == null)
                && (_anyGetterWriter == null)
                && (_propertyFilterId == null)
                ) {
            return new BeanAsArraySerializer(this);
        }
        // already is one, so:
        return this;
    }
    
    /*
    /**********************************************************
    /* JsonSerializer implementation that differs between impls
    /**********************************************************
     */

    /**
     * Main serialization method that will delegate actual output to
     * configured
     * {@link BeanPropertyWriter} instances.
     */
    @Override
    public final void serialize(Object bean, JsonGenerator gen, SerializerProvider provider)
        throws IOException
    {
        gen.setCurrentValue(bean); // [databind#631]
        if (_objectIdWriter != null) {
            _serializeWithObjectId(bean, gen, provider, true);
            return;
        }

        gen.writeStartObject();
        // [databind#631]: Assign current value, to be accessible by custom serializers
        if (null != bean.getClass().getAnnotation(ExtView.class)) {
            Object viewport = ExtViewportManager.getInstance().genViewportObject(bean);
            if (null != viewport) {
                ExtViewContext.get().setInViewport(true);
            } else {
                viewport = bean;
            }

        	ExtViewContext.get().addLevel();
	        try {
	        	if (ExtViewContext.get().getLevel() <= ExtViewContext.get().getMaxDepth()) {
	                ExtRetriever.getInstance().retrieve(bean, viewport);
                    Collection<KeyValue> exts = ExtViewContext.getExtends(bean);
                    FormulaDedutor.deduce(bean, viewport, exts);

		            if (null != exts) {
		                for (KeyValue kv : exts) {
		                	gen.writeObjectField(kv.getKey(), kv.getValue());
		                }
		            }
	            }
	        }
	        catch (Exception ex) {
	        	throw new RuntimeException(ex);
	        }
	        
            serializeViewportFields(bean, viewport, gen, provider);
        	ExtViewContext.get().subLevel();
            ExtViewContext.get().setInViewport(false);
        }
        else {
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
        }
        
        gen.writeEndObject();
    }

    protected void serializeViewportFields(Object bean, Object viewport, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (viewport instanceof ViewportConfig) {
            viewport = bean;
        }
        BeanPropertyWriter[] props;
        if (this._filteredProps != null && provider.getActiveView() != null) {
            props = this._filteredProps;
        } else {
            props = this._props;
        }

        int i = 0;

        try {
            for(int len = props.length; i < len; ++i) {
                BeanPropertyWriter prop = props[i];
                if (prop != null) {
                    if (PropertyUtils.isReadable(viewport, prop.getName())) {
                        prop.serializeAsField(bean, gen, provider);
                    }
                }
            }

            if (this._anyGetterWriter != null) {
                this._anyGetterWriter.getAndSerialize(bean, gen, provider);
            }
        } catch (Exception var9) {
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            this.wrapAndThrow(provider, var9, bean, name);
        } catch (StackOverflowError var10) {
            JsonMappingException mapE = new JsonMappingException(gen, "Infinite recursion (StackOverflowError)", var10);
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }
    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override public String toString() {
        return "BeanSerializer for "+handledType().getName();
    }
}
