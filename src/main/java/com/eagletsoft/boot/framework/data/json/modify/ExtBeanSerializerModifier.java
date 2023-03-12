package com.eagletsoft.boot.framework.data.json.modify;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.type.*;

public class ExtBeanSerializerModifier extends BeanSerializerModifier {

	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
			JsonSerializer<?> serializer) {
		if (serializer instanceof BeanSerializerBase) {
			return new ExtBeanSerializer((BeanSerializerBase)serializer);
		}
		else {
			return serializer;
		}
	}

	@Override
	public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config, CollectionType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return new ExtCollectionSerializer();
	}

	@Override
	public JsonSerializer<?> modifyArraySerializer(SerializationConfig config, ArrayType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return new ExtArraySerializer();
	}

	@Override
	public JsonSerializer<?> modifyCollectionLikeSerializer(SerializationConfig config, CollectionLikeType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return new ExtCollectionSerializer();
	}

	@Override
	public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return serializer;
	}

	@Override
	public JsonSerializer<?> modifyMapLikeSerializer(SerializationConfig config, MapLikeType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return serializer;
	}

	@Override
	public JsonSerializer<?> modifyEnumSerializer(SerializationConfig config, JavaType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return serializer;
	}

	@Override
	public JsonSerializer<?> modifyKeySerializer(SerializationConfig config, JavaType valueType,
			BeanDescription beanDesc, JsonSerializer<?> serializer) {
		return serializer;
	}

	
}
