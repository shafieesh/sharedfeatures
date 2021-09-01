package com.chainedminds.utilities;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XMLHelper {

    private static final XmlMapper xmlMapper = new XmlMapper();

    public static void main(String[] args) {

    }

    public static void generateMultiSitemap(String filePath, String webAddress, List<String> sitemapFiles) {

        Node root = new Node("sitemapindex");
        root.declarations.put("encoding", "UTF-8");
        root.attributes.put("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");

        for (String sitemapFile : sitemapFiles) {

            Node sitemapNode = new Node("sitemap");

            Node locNode = new Node("loc");
            locNode.value = webAddress + "/" + sitemapFile;

            sitemapNode.elements.add(locNode);

            root.elements.add(sitemapNode);
        }

        String xmlDocument = makeXML(root);

        Utilities.File.write(filePath, xmlDocument.getBytes());
    }

    public static List<String> generateSitemap(String path,
                                               String fileName,
                                               List<String> urls,
                                               Map<String, Set<String>> linkedImages) {

        List<String> sitemapNames = new ArrayList<>();

        for (int index = 0; index < urls.size(); index += 50_000) {

            String sitemapName = fileName + "-" + ((index / 50_000) + 1) + ".xml";

            String filePath = path + File.separator + sitemapName;

            sitemapNames.add(sitemapName);

            generateSitemap2(filePath, urls.subList(index, Math.min(index + 50_000, urls.size())), linkedImages);
        }

        return sitemapNames;
    }

    private static void generateSitemap2(String filePath, List<String> urls, Map<String, Set<String>> linkedImages) {

        Node root = new Node("urlset");
        root.declarations.put("encoding", "UTF-8");
        root.attributes.put("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
        root.attributes.put("xmlns:image", "http://www.google.com/schemas/sitemap-image/1.1");

        for (String url : urls) {

            Node urlNode = new Node("url");

            Node locNode = new Node("loc");
            locNode.value = url;

            urlNode.elements.add(locNode);

            for (String imageUrl : linkedImages.getOrDefault(url, new HashSet<>())) {

                Node imageNode = new Node("image:image");

                Node imageLoc = new Node("image:loc");
                imageLoc.value = imageUrl;

                imageNode.elements.add(imageLoc);

                urlNode.elements.add(imageNode);
            }

            root.elements.add(urlNode);

            //System.out.println(url);
        }

        String xmlDocument = makeXML(root);

        //System.out.println(xmlDocument);

        Utilities.File.write(filePath, xmlDocument.getBytes());
    }

    private static String makeXML(Node rootNode) {

        return makeXML(rootNode, 1);
    }

    private static String makeXML(Node rootNode, int level) {

        //String xml = "";
        StringBuilder xml = new StringBuilder();

        if (level == 1) {

            xml.append("<?xml version=\"1.0\"");

            for (String key : rootNode.declarations.keySet()) {

                xml.append(" ")
                        .append(key)
                        .append("=")
                        .append("\"")
                        .append(rootNode.declarations.get(key))
                        .append("\"");
            }

            xml.append("?>");

            xml.append("\r\n");
        }

        xml.append("<").append(rootNode.name);

        for (String key : rootNode.attributes.keySet()) {

            xml.append(" ")
                    .append(key)
                    .append("=")
                    .append("\"")
                    .append(rootNode.attributes.get(key))
                    .append("\"");
        }

        if (rootNode.elements.size() == 0) {

            if (rootNode.value != null) {

                xml.append(">");

                xml.append(rootNode.value);

                xml.append("</").append(rootNode.name).append(">");

            } else {

                xml.append("/>");
            }

        } else {

            xml.append(">");

            for (Node element : rootNode.elements) {

                xml.append("\r\n");

                xml.append(new String(new char[Math.max(0, level)]).replace("\0", "    "));

                xml.append(makeXML(element, level + 1));
            }

            xml.append("\r\n");

            xml.append(new String(new char[Math.max(0, level - 1)]).replace("\0", "    "));

            xml.append("</").append(rootNode.name).append(">");
        }

        return xml.toString();
    }

    public static byte[] getBytes(Object data) {

        try {

            byte[] dataBytes = xmlMapper.writeValueAsBytes(data);

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

            return xmlMapper.readValue(data, mappedClass);

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

    public static class Node {

        Node(String name) {

            this.name = name;
        }

        private final String name;
        Object value;
        List<Node> elements = new ArrayList<>();
        Map<String, String> attributes = new HashMap<>();

        Map<String, String> declarations = new HashMap<>();
    }
}