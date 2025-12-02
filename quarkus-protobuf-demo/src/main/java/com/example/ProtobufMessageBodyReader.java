package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.protobuf.Message;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
@Consumes("application/x-protobuf")
public class ProtobufMessageBodyReader implements MessageBodyReader<Message> {

    private static final Logger LOG = Logger.getLogger(ProtobufMessageBodyReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        boolean readable = Message.class.isAssignableFrom(type);
        LOG.infof("ProtobufMessageBodyReader.isReadable: type=%s, readable=%s", type.getSimpleName(), readable);
        return readable;
    }

    @Override
    public Message readFrom(Class<Message> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> headers,
            InputStream inputStream)
            throws IOException, WebApplicationException {
        LOG.infof("ProtobufMessageBodyReader.readFrom: parsing %s from InputStream", type.getSimpleName());
        try {
            Method parseFrom = type.getMethod("parseFrom", InputStream.class);
            Message message = (Message) parseFrom.invoke(null, inputStream);
            LOG.infof("ProtobufMessageBodyReader.readFrom: successfully parsed %s", type.getSimpleName());
            return message;
        } catch (Exception e) {
            LOG.errorf(e, "ProtobufMessageBodyReader.readFrom: failed to parse %s", type.getSimpleName());
            throw new WebApplicationException("Failed to parse protobuf message", e);
        }
    }
}