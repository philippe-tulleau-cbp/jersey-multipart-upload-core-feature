package com.archytasit.jersey.multipart.internal.valueproviders;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.internal.util.collection.NullableMultivaluedHashMap;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.model.Parameter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;

import com.archytasit.jersey.multipart.exception.FormDataParamException;
import com.archytasit.jersey.multipart.model.bodyparts.FormBodyPart;

public class SingleValueProvider extends AbstractEnhancedBodyPartsValueProvider<Object> {

    protected MultivaluedParameterExtractor<?> extractor;

    public SingleValueProvider(Parameter parameter, MultivaluedParameterExtractor<?> extractor) {
        super(parameter);
        this.extractor = extractor;
    }

    @Override
    protected Object applyToEnhancedBodyParts(ContainerRequest request, List<EnhancedBodyPart> enhancedBodyParts) {
        if (enhancedBodyParts.isEmpty()) {
            return null;
        }
        EnhancedBodyPart bp = enhancedBodyParts.get(0);
        switch (bp.getMode()) {
            case MESSAGE_BODY_WORKER:
                return applyMessageBodyWorker(bp, request.getWorkers());
            case EXTRACTOR:
                return applyExtractor(Stream.of(bp), request.getWorkers());
            default:
                return null;
        }
    }


    protected Object applyExtractor(Stream<EnhancedBodyPart> bp, MessageBodyWorkers workers) {
        MultivaluedMap paramValuesMap = new NullableMultivaluedHashMap();
        paramValuesMap.addAll(getParameterName(),
            bp.map(b -> asString(b, workers)).collect(Collectors.toList()));
        return extractor.extract(paramValuesMap);

    }

    private String asString(EnhancedBodyPart iBodyPart, MessageBodyWorkers workers) {
        if (iBodyPart.getBodyPart() instanceof FormBodyPart) {
            return ((FormBodyPart) iBodyPart.getBodyPart()).getDataBag();
        } else {
            return applyMessageBodyWorker(iBodyPart, String.class, String.class, MediaType.TEXT_PLAIN_TYPE, workers);
        }
    }

    protected Object applyMessageBodyWorker(EnhancedBodyPart bodyPart, MessageBodyWorkers workers) {
        return applyMessageBodyWorker(bodyPart, parameter.getRawType(), parameter.getType(), bodyPart.getMappedMediaType(), workers);
    }


    protected <T> T applyMessageBodyWorker(EnhancedBodyPart bodyPart, Class<T> rawType, Type type, MediaType customMediaType, MessageBodyWorkers workers) {

        MessageBodyReader<T> bodyReader = workers.getMessageBodyReader(
                rawType,
                type,
                parameter.getAnnotations(),
                customMediaType);
        if (bodyReader != null) {
            try {
                return (T) bodyReader.readFrom(rawType,
                        type,
                        parameter.getAnnotations(),
                        customMediaType,
                        bodyPart.getBodyPart().getHeaders(),
                        bodyPart.getBodyPart().getInputStream());
            } catch (final IOException e) {
                throw new FormDataParamException(e, getParameterName(), parameter.getDefaultValue());
            }
        }
        return null;
    }


}