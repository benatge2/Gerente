package com.example.gerenteapp;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class InformeController {

    public static void generarInforme() {
        Connection connection = null;
        try {
            // Establecer la conexión a la base de datos
            String url = "jdbc:mysql://localhost:3306/5_erronka1";
            String user = "root";
            String password = "1WMG2023";
            Connection conn = DriverManager.getConnection(url, user, password);

            // Cargar el archivo .jasper correctamente desde los recursos
            InputStream reportStream = InformeController.class.getResourceAsStream("/Reports/2Erronka5_Plantilla.jasper");

            if (reportStream == null) {
                throw new JRException("No se encontró el archivo .jasper en /Reports/");
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

            // Parámetros opcionales
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("nombreParametro", "ValorParametro");

            // Llenar el informe con datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            // Exportar a PDF
            String outputPath = "informe.pdf";  // Lo guardamos en la raíz del proyecto para evitar problemas de permisos
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            System.out.println("✅ PDF generado correctamente en: " + outputPath);

            // Cerrar la conexión
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
