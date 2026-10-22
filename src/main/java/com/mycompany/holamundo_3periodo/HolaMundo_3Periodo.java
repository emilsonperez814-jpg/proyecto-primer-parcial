package com.mycompany.holamundo_3periodo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HolaMundo_3Periodo {

    // validar si ya existe un correo
    public static boolean existeCorreo(Connection cn, String correo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Alumnos WHERE correo = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // validar si ya existe un id
    public static boolean existeId(Connection cn, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Alumnos WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // validar que el correo no lo tenga otro alumno
    public static boolean correoUsadoPorOtro(Connection cn, String correo, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Alumnos WHERE correo = ? AND id <> ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // obtener nombre y correo de un alumno por su id, devuelve null si no existe
    public static String[] obtenerAlumno(Connection cn, int id) throws SQLException {
        String sql = "SELECT nombre, correo FROM Alumnos WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("nombre"), rs.getString("correo")};
                }
                return null;
            }
        }
    }

    // mostrar la lista de alumnos en pantalla
    public static void listarAlumnos(Connection cn) throws SQLException {
        String sqlSelect = "SELECT id, nombre, correo FROM Alumnos";
        try (PreparedStatement ps = cn.prepareStatement(sqlSelect);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n=================================================");
            System.out.printf("%-3s | %-20s | %s%n", "ID", "Nombre", "Correo");
            System.out.println("=================================================");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String correo = rs.getString("correo");
                System.out.printf("%-3d | %-20s | %s%n", id, nombre, correo);
            }
            System.out.println("=================================================");
        }
    }

    // leer un numero entero del teclado, devuelve -1 si no es valido
    public static int leerEntero(Scanner sc) {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // validar que el correo tenga un formato basico correcto
    public static boolean correoValido(String correo) {
        return correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    // opcion 1: insertar un alumno nuevo
    public static void insertarAlumno(Connection cn, Scanner sc) throws SQLException {
        System.out.println("\nIngresar nuevo alumno");
        System.out.print("Nombre: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("El nombre no puede estar vacio");
            return;
        }

        System.out.print("Correo: ");
        String correo = sc.nextLine().trim();
        if (!correoValido(correo)) {
            System.out.println("El correo no tiene un formato valido");
            return;
        }
        if (existeCorreo(cn, correo)) {
            System.out.println("El alumno ya existe, no se inserto");
            return;
        }

        String sqlInsert = "INSERT INTO Alumnos(nombre, correo) VALUES(?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            if (ps.executeUpdate() > 0) {
                System.out.println("!Alumno insertado con exito!");
            }
        }
    }

    // opcion 3: actualizar un alumno (si se deja vacio, se conserva el valor actual)
    public static void actualizarAlumno(Connection cn, Scanner sc) throws SQLException {
        System.out.println("\nActualizar alumno");
        System.out.print("ID del alumno a modificar: ");
        int id = leerEntero(sc);
        if (id == -1) {
            System.out.println("ID invalido");
            return;
        }

        String[] actual = obtenerAlumno(cn, id);
        if (actual == null) {
            System.out.println("No existe el alumno que se quiere modificar");
            return;
        }

        System.out.print("Nuevo nombre (" + actual[0] + "): ");
        String nuevoNombre = sc.nextLine().trim();
        if (nuevoNombre.isEmpty()) {
            nuevoNombre = actual[0];
        }

        System.out.print("Nuevo correo (" + actual[1] + "): ");
        String nuevoCorreo = sc.nextLine().trim();
        if (nuevoCorreo.isEmpty()) {
            nuevoCorreo = actual[1];
        } else if (!correoValido(nuevoCorreo)) {
            System.out.println("El correo no tiene un formato valido");
            return;
        } else if (correoUsadoPorOtro(cn, nuevoCorreo, id)) {
            System.out.println("El correo " + nuevoCorreo + " ya lo tiene otro alumno");
            return;
        }

        String sqlUpdate = "UPDATE Alumnos SET nombre = ?, correo = ? WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, nuevoCorreo);
            ps.setInt(3, id);
            if (ps.executeUpdate() > 0) {
                System.out.println("!Alumno modificado con exito!");
            }
        }
    }

    // opcion 4: eliminar un alumno con confirmacion
    public static void eliminarAlumno(Connection cn, Scanner sc) throws SQLException {
        System.out.println("\nEliminar alumno");
        System.out.print("ID del alumno a eliminar: ");
        int id = leerEntero(sc);
        if (id == -1) {
            System.out.println("ID invalido");
            return;
        }

        String[] alumno = obtenerAlumno(cn, id);
        if (alumno == null) {
            System.out.println("No existe el alumno que se quiere eliminar");
            return;
        }

        System.out.print("Seguro que desea eliminar a " + alumno[0] + "? (s/n): ");
        String respuesta = sc.nextLine().trim();
        if (!respuesta.equalsIgnoreCase("s")) {
            System.out.println("Eliminacion cancelada");
            return;
        }

        String sqlDelete = "DELETE FROM Alumnos WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlDelete)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) {
                System.out.println("!Alumno eliminado con exito!");
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // obtener la conexion
        Connection cn = ConexionDB.getConexion();
        if (cn == null) {
            System.out.println("No se puede establecer la conexion");
            sc.close();
            return;
        }

        int opcion;
        do {
            System.out.println("\n=== MENU ALUMNOS ===");
            System.out.println("1. Ingresar nuevo alumno");
            System.out.println("2. Ver todos los alumnos");
            System.out.println("3. Actualizar alumno");
            System.out.println("4. Eliminar alumno");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opcion: ");
            opcion = leerEntero(sc);

            try {
                switch (opcion) {
                    case 1:
                        insertarAlumno(cn, sc);
                        break;
                    case 2:
                        listarAlumnos(cn);
                        break;
                    case 3:
                        actualizarAlumno(cn, sc);
                        break;
                    case 4:
                        eliminarAlumno(cn, sc);
                        break;
                    case 5:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opcion no valida");
                }
            } catch (SQLException e) {
                System.err.println("Ocurrio un error con la base de datos: " + e.getMessage());
            }
        } while (opcion != 5);

        // cerrar la conexion de manera obligatoria
        ConexionDB.cerrarConexion();
        sc.close();
    }
}