package com.example;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.protobuf.Message;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
@Produces("application/x-protobuf")
public class ProtobufMessageBodyWriter implements MessageBodyWriter<Message> {

    private static final Logger LOG = Logger.getLogger(ProtobufMessageBodyWriter.class);

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        boolean writeable = Message.class.isAssignableFrom(type);
        LOG.infof("ProtobufMessageBodyWriter.isWriteable: type=%s, writeable=%s", type.getSimpleName(), writeable);
        return writeable;
    }

    @Override
    public void writeTo(Message message, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream)
            throws IOException, WebApplicationException {
        LOG.infof("ProtobufMessageBodyWriter.writeTo: writing %s to OutputStream", type.getSimpleName());
        message.writeTo(entityStream);
        LOG.infof("ProtobufMessageBodyWriter.writeTo: successfully wrote %s", type.getSimpleName());
    }
}