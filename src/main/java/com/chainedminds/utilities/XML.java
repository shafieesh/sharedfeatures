package com.chainedminds.utilities;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class XML {

    public static String toXml(String method, String xmlns, Object object) {

        if (object == null) {

            return null;
        }

        try {

            String key;
            Object value;

            String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
            xml += "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
            xml += "<soap:Body>";
            xml += "<" + method + " xmlns=\"" + xmlns + "\">";

            Field[] fields = object.getClass().getDeclaredFields();

            for (Field field : fields) {

                key = field.getName();
                value = field.get(object);

                if (value != null) {

                    xml += "<" + key + ">" + value + "</" + key + ">";

                } else {

                    xml += "<" + key + "/>";
                }
            }

            xml += "</" + method + ">";
            xml += "</soap:Body>";
            xml += "</soap:Envelope>";

            return xml;

        } catch (Exception e) {

            _Log.error("XmlException", e);
        }

        return null;
    }

    public static <T> T parse(String xml, Class<T> clazz) {

        if (xml == null) {

            return null;
        }

        try {

            Map<String, String> mapping = new HashMap<>();

            Pair pair = new Pair();

            int firstIndex;
            String key, value;

            Stack<String> stack = new Stack<>();

            while (pair.startPointer != -1) {

                firstIndex = xml.indexOf("<", pair.startPointer);

                if (firstIndex == -1) {

                    break;
                }

                pair.startPointer = firstIndex + 1;
                pair.endPointer = xml.indexOf(">", pair.startPointer);

                key = xml.substring(pair.startPointer, pair.endPointer).toLowerCase();

                // Cleaning found field

                if (key.contains(":")) {

                    key = key.substring(key.indexOf(":") + 1);
                }
                if (key.contains(" ")) {

                    key = key.substring(0, key.indexOf(" "));
                }

                // Checking for MetaData

                if (xml.substring(pair.startPointer, pair.endPointer).contains("?")) {

                    pair.startPointer = pair.endPointer + 1;

                    continue;
                }

                // Checking for EarlyClose

                int clazzLength = key.length();

                if (key.startsWith("/", clazzLength - 1)) {

                    key = key.substring(0, clazzLength - 1);
                }

                if (xml.startsWith("/", pair.startPointer)) {

                    if (key.equals(stack.peek())) {

                        int a = xml.lastIndexOf(">", pair.startPointer) + 1;

                        value = xml.substring(a, pair.startPointer - 1);

                        mapping.put(key, value);
                    }

                    stack.pop();

                } else {

                    stack.push(key);
                }
            }

            T myObject = clazz.newInstance();

            Field[] fields = myObject.getClass().getDeclaredFields();

            for (Field field : fields) {

                key = field.getName().toLowerCase();

                if (mapping.containsKey(key)) {

                    Class<?> classType = field.getType();

                    try {

                        if (classType.equals(Long.class) || classType.equals(long.class)) {

                            field.setLong(myObject, Long.parseLong(mapping.get(key)));

                            continue;
                        }

                        if (classType.equals(Integer.class) || classType.equals(int.class)) {

                            field.setInt(myObject, Integer.parseInt(mapping.get(key)));

                            continue;
                        }

                        if (classType.equals(String.class)) {

                            field.set(myObject, mapping.get(key));

                            continue;
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }

            return myObject;

        } catch (Exception e) {

            _Log.error("XmlException", e, xml);
        }

        return null;
    }

    static class Pair {

        int startPointer;
        int endPointer;
    }
}