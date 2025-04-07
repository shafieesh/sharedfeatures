package com.chainedminds.utilities.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Json {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            //.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void setSerializer() {

        SimpleModule module = new SimpleModule();

        module.addSerializer(List.class, new JsonSerializer<List>() {
            @Override
            public void serialize(List value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

                gen.writeStartArray();

                for (Object object : value) {

                    gen.writeObject(object);
                }

                gen.writeEndArray();
            }
        });

        module.addSerializer(Map.class, new JsonSerializer<Map>() {
            @Override
            public void serialize(Map value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

                gen.writeStartObject();

                value.forEach((o, o2) -> {

                    try {

                        gen.writeFieldName(o.toString());

                        gen.writeObject(o2);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                gen.writeEndObject();
            }
        });

        module.addSerializer(Set.class, new JsonSerializer<Set>() {
            @Override
            public void serialize(Set value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

                gen.writeStartArray();

                for (Object object : value) {

                    gen.writeObject(object);
                }

                gen.writeEndArray();
            }
        });

        objectMapper.registerModule(module);
    }

    //-------------------------------------------------------

    public static byte[] getBytes(Object data) {

        try {

            return getBytesUnsafe(data);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static byte[] getBytesUnsafe(Object data) throws JsonException {

        try {

            byte[] dataBytes = objectMapper.writeValueAsBytes(data);

            if (dataBytes.length == 4 &&
                    dataBytes[0] == 110 &&
                    dataBytes[1] == 117 &&
                    dataBytes[2] == 108 &&
                    dataBytes[3] == 108) {

                return null;
            }

            return dataBytes;

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }

    public static String getString(Object data) {

        try {

            return getStringUnsafe(data);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static String getStringUnsafe(Object data) throws JsonException {

        byte[] jsonBytes = getBytesUnsafe(data);

        if (jsonBytes != null) {

            return new String(jsonBytes);
        }

        return null;
    }

    //-------------------------------------------------------

    public static <T> T getObject(String data) {

        try {

            return getObjectUnsafe(data);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <T> T getObjectUnsafe(String data) throws JsonException {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getObjectUnsafe(dataBytes);
        }

        return null;
    }

    public static <T> T getObject(byte[] data) {

        try {

            return getObjectUnsafe(data);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <T> T getObjectUnsafe(byte[] data) throws JsonException {

        try {

            return objectMapper.readValue(data, new TypeReference<T>() {
            });

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }

    //-------------------------------------------------------

    public static JsonNode getNode(String data) {

        try {

            return getNodeUnsafe(data);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static JsonNode getNodeUnsafe(String data) throws JsonException {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getNodeUnsafe(dataBytes);
        }

        return null;
    }

    public static JsonNode getNode(byte[] data) {

        try {

            return getNodeUnsafe(data);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static JsonNode getNodeUnsafe(byte[] data) throws JsonException {

        try {

            return objectMapper.readTree(data);

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }

    //-------------------------------------------------------

    public static <T> T getObject(String data, Class<T> mappedClass) {

        try {

            return getObjectUnsafe(data, mappedClass);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <T> T getObjectUnsafe(String data, Class<T> mappedClass) throws JsonException {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getObjectUnsafe(dataBytes, mappedClass);
        }

        return null;
    }

    public static <T> T getObject(byte[] data, Class<T> mappedClass) {

        try {

            return getObjectUnsafe(data, mappedClass);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <T> T getObjectUnsafe(byte[] data, Class<T> mappedClass) throws JsonException {

        try {

            return objectMapper.readValue(data, mappedClass);

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }

    //-------------------------------------------------------

    public static <T> List<T> getList(String data, Class<T> mappedClass) {

        try {

            return getListUnsafe(data, mappedClass);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <T> List<T> getListUnsafe(String data, Class<T> mappedClass) throws JsonException {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getListUnsafe(dataBytes, mappedClass);
        }

        return null;
    }

    public static <T> List<T> getList(byte[] data, Class<T> mappedClass) {

        try {

            return getListUnsafe(data, mappedClass);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <T> List<T> getListUnsafe(byte[] data, Class<T> mappedClass) throws JsonException {

        try {

            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, mappedClass);

            return objectMapper.readValue(data, listType);

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }

    //-------------------------------------------------------

    public static <K, V> Map<K, V> getMap(String data, Class<K> keyClass, Class<V> valueClass) {

        try {

            return getMapUnsafe(data, keyClass, valueClass);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <K, V> Map<K, V> getMapUnsafe(String data, Class<K> keyClass, Class<V> valueClass) throws JsonException {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getMapUnsafe(dataBytes, keyClass, valueClass);
        }

        return null;
    }

    public static <K, V> Map<K, V> getMap(byte[] data, Class<K> keyClass, Class<V> valueClass) {

        try {

            return getMapUnsafe(data, keyClass, valueClass);

        } catch (JsonException ignore) {

            return null;
        }
    }

    public static <K, V> Map<K, V> getMapUnsafe(byte[] data, Class<K> keyClass, Class<V> valueClass) throws JsonException {

        try {

            MapType mapType = objectMapper.getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);

            return objectMapper.readValue(data, mapType);

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }

    //-------------------------------------------------------

    public static <T> T[] getArray(String data, Class<T> mappedClass) {

        try {

            return getArrayUnsafe(data, mappedClass);

        } catch (JsonException e) {

            return null;
        }
    }

    public static <T> T[] getArrayUnsafe(String data, Class<T> mappedClass) throws JsonException {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getArrayUnsafe(dataBytes, mappedClass);
        }

        return null;
    }

    public static <T> T[] getArray(byte[] data, Class<T> mappedClass) {

        try {

            return getArrayUnsafe(data, mappedClass);

        } catch (JsonException e) {

            return null;
        }
    }

    public static <T> T[] getArrayUnsafe(byte[] data, Class<T> mappedClass) throws JsonException {

        try {

            ArrayType listType = objectMapper.getTypeFactory()
                    .constructArrayType(mappedClass);

            return objectMapper.readValue(data, listType);

        } catch (Exception e) {

            throw new JsonException(e);
        }
    }
}