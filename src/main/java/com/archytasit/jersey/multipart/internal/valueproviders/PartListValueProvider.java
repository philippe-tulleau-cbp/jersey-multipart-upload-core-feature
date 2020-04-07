package com.archytasit.jersey.multipart.internal.valueproviders;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.glassfish.jersey.model.Parameter;
import org.glassfish.jersey.server.ContainerRequest;


import com.archytasit.jersey.multipart.model.MultiPart;
import com.archytasit.jersey.multipart.model.bodyparts.IBodyPart;
import com.archytasit.jersey.multipart.utils.ClassUtils;

public class PartListValueProvider<T extends IBodyPart> extends AbstractBodyPartsValueProvider<Collection<T>> {


    private final Class<?> genericClass;

    public PartListValueProvider(Parameter parameter, Class<?> genericClass) {
        super(parameter);
        this.genericClass = genericClass;
    }

    @Override
    protected Collection<T> applyToBodyParts(ContainerRequest request, List<IBodyPart> enhancedBodyParts) {
        return (Collection<T>) enhancedBodyParts.stream()
                    .filter(Objects::nonNull)
                    .filter(b -> this.genericClass.isAssignableFrom(b.getClass()))
                    .collect(ClassUtils.getStreamCollector(parameter.getRawType()));
    }
}