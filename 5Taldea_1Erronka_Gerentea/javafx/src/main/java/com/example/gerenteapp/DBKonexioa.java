package com.example.gerenteapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBKonexioa {
    // Datu basearen informazioa sartu
    private static final String Db_izena = "jdbc:mysql://192.168.115.155:3306/5_erronka1";
    private static final String Db_erabiltzailea = "admin";
    private static final String Db_pasahitza = "5_erronka1";

    // Datu base locala
    //private static final String Db_izena = "jdbc:mysql://localhost:3306/5_erronka1";
    //private static final String Db_erabiltzailea = "root";
    //private static final String Db_pasahitza = "1WMG2023";

    // konexioa hasieran null dela ziurtatu
    private static Connection connection = null;

    // konexioa sortzeko funtzioa lortu
    public static Connection getKonexioa() throws SQLException {
        if (connection == null || connection.isClosed()) {  //konexioa null bada eta itxita badago funtziora sartu
            try {
                connection = DriverManager.getConnection(Db_izena, Db_erabiltzailea, Db_pasahitza); //konexioa egin
                System.out.println("Konexioa zuzen egin da."); // zezen egin dela ziurtatzeko mezua
            } catch (SQLException e) { //errorea idenfikatzeko excepzioa
                System.err.println("Errorea konexioa egiterakoan: " + e.getMessage()); // errore mezua pantailaratu beharrekoa
                throw e;
            }
        }
        return connection;
    }


    public static void itxiKonexioa() { //konexioa ixteko funtzioa
        if (connection != null) { //konexioa badago bakarrik, ez badago ezin da itxi
            try {
                connection.close(); //konexioa itxi
                System.out.println("Konexioa itxi da."); //konexioa itxi eta gero mezua erabiltzaileak jakiteko
            } catch (SQLException e) {
                System.err.println("Errorea konexioa ixtean: " + e.getMessage()); //errorea badago mezua, errorea identifikatzeko
            }
        }
    }
}