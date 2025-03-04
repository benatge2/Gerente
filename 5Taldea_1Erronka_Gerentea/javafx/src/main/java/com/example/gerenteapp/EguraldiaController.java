package com.example.gerenteapp;

import javafx.application.Application;
import javafx.stage.Stage;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EguraldiaController extends Application {

    @Override
    public void start(Stage primaryStage) {
    }

    private static final String URL_STRING = "https://www.aemet.es/xml/municipios/localidad_20076.xml";
    private static final String OUTPUT_PATH = "../../EguraldiaXml/eguraldia.xml";
    private static final String BATCH_FILE_PATH = "update.bat";
    private static final String  PULL_FILE_PATH = "pull.bat";

    public void descargarYProcesarXml() {
        verificarYEjecutar(OUTPUT_PATH, PULL_FILE_PATH);
        try {
            URL url = new URL(URL_STRING);
            InputStream inputStream = url.openStream();
            String xmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            inputStream.close();

            if (!xmlContent.trim().startsWith("<")) {
                System.err.println("El contenido descargado no es XML válido.");
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            Document newDocument = builder.newDocument();
            Element rootElement = newDocument.createElement("prediccion");
            newDocument.appendChild(rootElement);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression diaExp = xpath.compile("//prediccion/dia");
            NodeList diaNodes = (NodeList) diaExp.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < diaNodes.getLength(); i++) {
                Node diaNode = diaNodes.item(i);
                Element diaElement = newDocument.createElement("dia");
                rootElement.appendChild(diaElement);

                String fecha = diaNode.getAttributes().getNamedItem("fecha").getNodeValue();
                diaElement.setAttribute("fecha", fecha);

                XPathExpression maximaExp = xpath.compile("temperatura/maxima");
                XPathExpression minimaExp = xpath.compile("temperatura/minima");
                String maxima = maximaExp.evaluate(diaNode);
                String minima = minimaExp.evaluate(diaNode);

                Element temperaturaElement = newDocument.createElement("temperatura");
                diaElement.appendChild(temperaturaElement);
                Element maximaElement = newDocument.createElement("maxima");
                maximaElement.setTextContent(maxima);
                temperaturaElement.appendChild(maximaElement);
                Element minimaElement = newDocument.createElement("minima");
                minimaElement.setTextContent(minima);
                temperaturaElement.appendChild(minimaElement);

                // --- Obtener solo el nodo de prob_precipitacion con etiqueta 0-24 ---
                Node probPrecipNode = (Node) xpath.evaluate("prob_precipitacion[@periodo='00-24']", diaNode, XPathConstants.NODE);
                if (probPrecipNode != null) {
                    Element precipElement = newDocument.createElement("prob_precipitacion");
                    precipElement.setAttribute("periodo", "00-24");
                    precipElement.setTextContent(probPrecipNode.getTextContent().trim());
                    diaElement.appendChild(precipElement);
                }

                // --- Obtener solo el nodo de viento con etiqueta 0-24 ---
                Node vientoNode = (Node) xpath.evaluate("viento[@periodo='00-24']", diaNode, XPathConstants.NODE);
                if (vientoNode != null) {
                    Element vientoElement = newDocument.createElement("viento");
                    vientoElement.setAttribute("periodo", "00-24");
                    NodeList vientoChildNodes = vientoNode.getChildNodes();
                    for (int k = 0; k < vientoChildNodes.getLength(); k++) {
                        Node childNode = vientoChildNodes.item(k);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element childElement = newDocument.createElement(childNode.getNodeName());
                            childElement.setTextContent(childNode.getTextContent().trim());
                            vientoElement.appendChild(childElement);
                        }
                    }
                    diaElement.appendChild(vientoElement);
                }

                // --- Obtener solo el nodo de cota_nieve_prov con etiqueta 0-24 ---
                Node nieveNode = (Node) xpath.evaluate("cota_nieve_prov[@periodo='00-24']", diaNode, XPathConstants.NODE);
                if (nieveNode != null) {
                    Element nieveElement = newDocument.createElement("cota_nieve_prov");
                    nieveElement.setAttribute("periodo", "00-24");
                    nieveElement.setTextContent(nieveNode.getTextContent().trim());
                    diaElement.appendChild(nieveElement);
                }

                // --- Obtener estado del cielo ---
                Node estadoCieloNode = (Node) xpath.evaluate("estado_cielo[@periodo='00-24']", diaNode, XPathConstants.NODE);
                if (estadoCieloNode != null) {
                    Element estadoCieloElement = newDocument.createElement("estado_cielo");
                    String descripcion = estadoCieloNode.getAttributes().getNamedItem("descripcion").getNodeValue();
                    estadoCieloElement.setAttribute("descripcion", descripcion);
                    estadoCieloElement.setTextContent(estadoCieloNode.getTextContent().trim());
                    diaElement.appendChild(estadoCieloElement);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(newDocument);
            StreamResult result = new StreamResult(new File(OUTPUT_PATH));
            transformer.transform(source, result);

            System.out.println("XML descargado, procesado y guardado exitosamente.");

            ejecutarBatchFile(BATCH_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ejecutarBatchFile(String filePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("C:\\Windows\\System32\\cmd.exe", "/c", filePath);
            processBuilder.directory(new File(filePath).getParentFile());
            Process process = processBuilder.start();

            // Capturar la salida estándar y de error
            InputStream is = process.getInputStream();
            InputStream es = process.getErrorStream();

            new Thread(() -> {
                try {
                    int value;
                    while ((value = is.read()) != -1) {
                        System.out.print((char) value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try {
                    int value;
                    while ((value = es.read()) != -1) {
                        System.err.print((char) value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            int exitCode = process.waitFor();
            System.out.println("Archivo .bat ejecutado con código de salida: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void verificarYEjecutar(String carpeta, String rutaBat) {
        File directorio = new File(carpeta);

        if (!directorio.exists()) {
            System.out.println("La carpeta no existe. Ejecutando el .bat...");

            try {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", rutaBat);
                processBuilder.start();
            } catch (IOException e) {
                System.err.println("Error al ejecutar el archivo .bat: " + e.getMessage());
            }
        } else {
            System.out.println("La carpeta ya existe.");
        }
    }
}
