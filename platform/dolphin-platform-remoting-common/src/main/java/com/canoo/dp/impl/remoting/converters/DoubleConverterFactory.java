/*
 * Copyright 2015-2017 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.canoo.dp.impl.remoting.converters;

import com.canoo.platform.remoting.spi.converter.Converter;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class DoubleConverterFactory extends AbstractConverterFactory {

    public final static int FIELD_TYPE_DOUBLE = 6;

    private final static Converter CONVERTER = new AbstractNumberConverter<Double>() {

        @Override
        public Double convertFromDolphin(Number value) {
            return value == null ? null : value.doubleValue();
        }

        @Override
        public Number convertToDolphin(Double value) {
            return value;
        }
    };


    @Override
    public boolean supportsType(Class<?> cls) {
        return double.class.equals(cls) || Double.class.equals(cls);
    }

    @Override
    public List<Class> getSupportedTypes() {
        return new ArrayList<>(Arrays.asList(double.class, Double.class));
    }

    @Override
    public int getTypeIdentifier() {
        return FIELD_TYPE_DOUBLE;
    }

    @Override
    public Converter getConverterForType(Class<?> cls) {
        return CONVERTER;
    }
}
