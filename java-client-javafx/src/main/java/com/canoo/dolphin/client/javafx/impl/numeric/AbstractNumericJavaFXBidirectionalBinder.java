package com.canoo.dolphin.client.javafx.impl.numeric;

import com.canoo.dolphin.client.javafx.BidirectionalConverter;
import com.canoo.dolphin.client.javafx.Binding;
import com.canoo.dolphin.client.javafx.NumericJavaFXBidirectionaBinder;
import com.canoo.dolphin.client.javafx.impl.DefaultJavaFXBinder;
import com.canoo.dolphin.mapping.Property;
import javafx.beans.value.ChangeListener;

/**
 * Created by hendrikebbers on 29.09.15.
 */
public abstract class AbstractNumericJavaFXBidirectionalBinder<S extends Number> extends DefaultJavaFXBinder<Number> implements NumericJavaFXBidirectionaBinder<S> {


    private final javafx.beans.property.Property<Number> javaFxProperty;

    public AbstractNumericJavaFXBidirectionalBinder(final javafx.beans.property.Property<Number> javaFxProperty) {
        super(javaFxProperty);
        this.javaFxProperty = javaFxProperty;
    }

    @Override
    public <T> Binding bidirectionalTo(final Property<T> property, BidirectionalConverter<T, Number> converter) {
        final Binding unidirectionalBinding = to(property, converter);
        final ChangeListener<Number> listener = (obs, oldVal, newVal) -> property.set(converter.convertBack(newVal));
        javaFxProperty.addListener(listener);
        return () -> {
            javaFxProperty.removeListener(listener);
            unidirectionalBinding.unbind();
        };
    }

    @Override
    public <T> Binding bidirectionalToNumeric(Property<T> property, BidirectionalConverter<T, S> converter) {
        final Binding unidirectionalBinding = to(property, converter);

        final ChangeListener<Number> listener = (obs, oldVal, newVal) -> property.set(converter.convertBack(convertNumber(newVal)));
        javaFxProperty.addListener(listener);
        return () -> {
            javaFxProperty.removeListener(listener);
            unidirectionalBinding.unbind();
        };
    }
}
