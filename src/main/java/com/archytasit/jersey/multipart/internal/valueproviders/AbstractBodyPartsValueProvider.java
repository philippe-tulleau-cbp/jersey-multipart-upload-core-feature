package com.archytasit.jersey.multipart.internal.valueproviders;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.model.Parameter;
import org.glassfish.jersey.server.ContainerRequest;

import com.archytasit.jersey.multipart.LoggingWrapper;
import com.archytasit.jersey.multipart.annotations.FormDataParam;
import com.archytasit.jersey.multipart.model.MultiPart;
import com.archytasit.jersey.multipart.model.bodyparts.IBodyPart;
import com.archytasit.jersey.multipart.utils.HeadersUtils;

public abstract class AbstractBodyPartsValueProvider<T> extends AbstractMultiPartValueProvider<T> {


    private String paramName;
    protected FormDataParam marker;
    protected Parameter parameter;
    private Predicate<IBodyPart> partNatureFilter;

    public AbstractBodyPartsValueProvider(Parameter parameter) {
        this.parameter = parameter;
        if (parameter.getSourceAnnotation().annotationType() == FormDataParam.class) {
            this.marker = (FormDataParam) parameter.getSourceAnnotation();
        } else {
            this.marker = parameter.getAnnotation(FormDataParam.class);
        }
        this.paramName = this.marker.value();
        this.partNatureFilter = preparePartNatureFilter(this.marker.filter());
    }


    @Override
    protected final T apply(ContainerRequest request, MultiPart entity) {
        List<IBodyPart> enhancedBodyParts = entity.getBodyParts().stream()
                .filter(Objects::nonNull)
                .filter(b -> paramName.equals(b.getFieldName()))
                .filter(this.partNatureFilter)
                .collect(Collectors.toList());
        return applyToBodyParts(request, enhancedBodyParts);
    }

    protected abstract T applyToBodyParts(ContainerRequest request, List<IBodyPart> enhancedBodyParts);


    protected String getParameterName() {
        return paramName;
    }


    private Predicate<IBodyPart> preparePartNatureFilter(FormDataParam.Filter filter) {
        switch (filter) {
            case FORMFIELD:
                return IBodyPart::isFormField;
            case ATTACHMENT:
                Predicate<IBodyPart> predicate = IBodyPart::isFormField;
                return predicate.negate();
            default :
                return (b) -> true;
        }
    }


}
