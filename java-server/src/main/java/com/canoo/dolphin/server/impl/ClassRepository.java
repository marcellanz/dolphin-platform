package com.canoo.dolphin.server.impl;

import com.canoo.dolphin.server.PresentationModelBuilder;
import org.opendolphin.core.Attribute;
import org.opendolphin.core.PresentationModel;
import org.opendolphin.core.Tag;
import org.opendolphin.core.server.ServerDolphin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassRepository {

    public static enum FieldType { UNKNOWN, BASIC_TYPE, ENUM, DOLPHIN_BEAN }
    private static final String PM_TYPE = ClassRepository.class.getName();

    private final Map<String, Field> fieldMap = new HashMap<>();
    private final Map<Class<?>, PresentationModel> classToPresentationModel = new HashMap<>();

    private ServerDolphin dolphin;

    public ClassRepository(ServerDolphin dolphin) {
        this.dolphin = dolphin;
    }

    public void register(final Class<?> beanClass) {
        if (classToPresentationModel.containsKey(beanClass)) {
            return;
        }

        final String id = DolphinUtils.getDolphinPresentationModelTypeForClass(beanClass);
        final PresentationModelBuilder builder = new PresentationModelBuilder(dolphin)
                .withType(PM_TYPE)
                .withId(id);

        if (beanClass.isEnum()) {
            final Object[] values = beanClass.getEnumConstants();
            for (int i = 0, n = values.length; i < n; i++) {
                builder.withAttribute(Integer.toString(i), values[i].toString());
            }
        } else {
            DolphinUtils.forAllProperties(beanClass, new DolphinUtils.FieldIterator() {
                @Override
                public void run(Field field, String attributeName) {
                    fieldMap.put(beanClass.getName() + "." + attributeName, field);
                    builder.withAttribute(attributeName, FieldType.UNKNOWN.ordinal(), Tag.VALUE_TYPE);
                    builder.withAttribute(attributeName, null, Tag.VALUE);
                }
            });

            DolphinUtils.forAllObservableLists(beanClass, new DolphinUtils.FieldIterator() {
                @Override
                public void run(Field field, String attributeName) {
                    fieldMap.put(beanClass.getName() + "." + attributeName, field);
                    builder.withAttribute(attributeName, FieldType.UNKNOWN.ordinal(), Tag.VALUE_TYPE);
                    builder.withAttribute(attributeName, null, Tag.VALUE);
                }
            });
        }

        final PresentationModel createdPresentationModel = builder.create();
        classToPresentationModel.put(beanClass, createdPresentationModel);
    }

    public FieldType getFieldType(Attribute fieldAttribute) {
        final Attribute classAttribute = findClassAttribute(fieldAttribute, Tag.VALUE_TYPE);
        return calculateFieldType(classAttribute);
    }

    public FieldType getFieldType(Class<?> beanClass, String attributeName) {
        final Attribute classAttribute = findClassAttribute(beanClass, attributeName, Tag.VALUE_TYPE);
        return calculateFieldType(classAttribute);
    }

    public Class<?> getFieldClass(Attribute fieldAttribute) {
        final Attribute classAttribute = findClassAttribute(fieldAttribute, Tag.VALUE);
        return calculateFieldClass(classAttribute);
    }

    public Class<?> getFieldClass(Class<?> beanClass, String attributeName) {
        final Attribute classAttribute = findClassAttribute(beanClass, attributeName, Tag.VALUE);
        return calculateFieldClass(classAttribute);
    }

    public FieldType calculateFieldTypeFromValue(Attribute fieldAttribute, Object value) {
        final FieldType fieldType = calculateFieldType(value);
        if (fieldType != FieldType.UNKNOWN) {
            findClassAttribute(fieldAttribute, Tag.VALUE_TYPE).setValue(fieldType.ordinal());
            findClassAttribute(fieldAttribute, Tag.VALUE).setValue(value.getClass().getName());
            if (fieldType == FieldType.ENUM) {
                register(value.getClass());
            }
        }
        return fieldType;
    }

    public FieldType calculateFieldTypeFromValue(Class<?> beanClass, String attributeName, Object value) {
        final FieldType fieldType = calculateFieldType(value);
        if (fieldType != FieldType.UNKNOWN) {
            findClassAttribute(beanClass, attributeName, Tag.VALUE_TYPE).setValue(fieldType.ordinal());
            findClassAttribute(beanClass, attributeName, Tag.VALUE).setValue(value.getClass().getName());
        }
        return fieldType;
    }

    public Field getField(Class<?> beanClass, String attributeName) {
        return fieldMap.get(beanClass.getName() + "." + attributeName);
    }




    private Attribute findClassAttribute(Attribute fieldAttribute, Tag tag) {
        final String classModelId = fieldAttribute.getPresentationModel().getPresentationModelType();
        final PresentationModel classModel = dolphin.findPresentationModelById(classModelId);
        return classModel.findAttributeByPropertyNameAndTag(fieldAttribute.getPropertyName(), tag);
    }

    private Attribute findClassAttribute(Class<?> beanClass, String attributeName, Tag tag) {
        final PresentationModel classModel = classToPresentationModel.get(beanClass);
        return classModel.findAttributeByPropertyNameAndTag(attributeName, tag);
    }

    private FieldType calculateFieldType(Attribute classAttribute) {
        try {
            return FieldType.values()[(Integer)classAttribute.getValue()];
        } catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
            // do nothing
        }
        return FieldType.UNKNOWN;
    }

    private Class<?> calculateFieldClass(Attribute classAttribute) {
        try {
            return Class.forName((String) classAttribute.getValue());
        } catch (NullPointerException | ClassCastException | ClassNotFoundException ex) {
            // do nothing
        }
        return null;
    }

    private static FieldType calculateFieldType(Object value) {
        if (value == null) {
            return FieldType.UNKNOWN;
        }

        final Class<?> clazz = value.getClass();
        if (DolphinUtils.isBasicType(clazz)) {
            return FieldType.BASIC_TYPE;
        }
        if (clazz.isEnum()) {
            return FieldType.ENUM;
        }
        return FieldType.DOLPHIN_BEAN;
    }

}