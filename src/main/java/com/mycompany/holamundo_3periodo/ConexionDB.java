package com.mycompany.holamundo_3periodo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Emilson
 */
public class ConexionDB {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=AvanzadaII;encrypt=true;trustServerCertificate=true";
    private static final String USUARIO = "sa";
    private static final String CLAVE = "12345";

    private static Connection conexion = null;

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
                System.out.println("->Conexion exitosa a Sql");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            conexion = null;
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("->La conexion con SQL fue Cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion: " + e.getMessage());
        }
    }
}