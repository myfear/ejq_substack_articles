package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class EncryptedMessageBodyReader implements MessageBodyReader<SecretMessage> {

    @Inject
    CryptoService cryptoService;
    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(SecretMessage.class);
    }

    @Override
    public SecretMessage readFrom(Class<SecretMessage> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {
        ObjectNode json = objectMapper.readValue(entityStream, ObjectNode.class);
        try {
            String encrypted = json.get("message").asText();
            String decrypted = cryptoService.decrypt(encrypted);
            SecretMessage result = new SecretMessage();
            result.message = decrypted;
            return result;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to decrypt message", 400);
        }
    }
}