package com.chainedminds.utilities.json;

import com.chainedminds.utilities.XML;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            //.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static {

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

    public static byte[] getBytes(Object data) {

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

            System.err.println("ORIGINAL DATA : \n" + XML.toXml("-", "-", data));

            e.printStackTrace();
        }

        return null;
    }

    public static String getString(Object data) {

        byte[] jsonBytes = getBytes(data);

        if (jsonBytes != null) {

            return new String(jsonBytes);
        }

        return null;
    }

    public static <T> T getObject(byte[] data, Class<T> mappedClass) {

        try {

            if (mappedClass == JsonNode.class) {

                return (T) objectMapper.readTree(data);

            } else {

                return objectMapper.readValue(data, mappedClass);
            }

        } catch (Exception e) {

            System.err.println("ORIGINAL DATA : \n" + new String(data));

            e.printStackTrace();
        }

        return null;
    }

    public static <T> T getObject(String data, Class<T> mappedClass) {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getObject(dataBytes, mappedClass);
        }

        return null;
    }

    public static <T> List<T> getList(byte[] data, Class<T> mappedClass) {

        try {

            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, mappedClass);

            return objectMapper.readValue(data, listType);

        } catch (Exception e) {

            System.err.println("ORIGINAL DATA : \n" + new String(data));

            e.printStackTrace();
        }

        return null;
    }

    public static <T> T[] getArray(byte[] data, Class<T> mappedClass) {

        try {

            ArrayType listType = objectMapper.getTypeFactory()
                    .constructArrayType(mappedClass);

            return objectMapper.readValue(data, listType);

        } catch (Exception e) {

            System.err.println("ORIGINAL DATA : \n" + new String(data));

            e.printStackTrace();
        }

        return null;
    }

    public static <T> List<T> getList(String data, Class<T> mappedClass) {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getList(dataBytes, mappedClass);
        }

        return null;
    }

    public static <T> T[] getArray(String data, Class<T> mappedClass) {

        if (data != null) {

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            return getArray(dataBytes, mappedClass);
        }

        return null;
    }
}