package es.studium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class proyecto extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private JTextField txtExtension;
    private JButton btnBuscar;
    private JList<String> listResultados;
    private DefaultListModel<String> listModel;

    public proyecto() {
        // Configuración del JFrame principal
        setTitle("Mis Ficheros");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Modelo y Lista para mostrar los resultados de búsqueda
        listModel = new DefaultListModel<>();
        listResultados = new JList<>(listModel);
        listResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listResultados);

        // Panel inferior para el campo de texto y botón
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        txtExtension = new JTextField(15);
        btnBuscar = new JButton("Buscar");
        panelInferior.add(txtExtension);
        panelInferior.add(btnBuscar);

        // Añadir elementos a la ventana principal
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Evento 1: Clic en el botón Buscar
        btnBuscar.addActionListener(e -> ejecutarBusqueda());

        // Evento 2: Doble clic sobre un elemento del listado
        listResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ejecutarSeleccionado();
                }
            }
        });
    }

    /**
     * Busca los ficheros en todas las unidades del sistema según la extensión dada.
     */
    private void ejecutarBusqueda() {
        String ext = txtExtension.getText().trim();
        if (ext.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduce una extensión (ej: dll, exe, txt).", 
                                          "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpiar resultados previos
        listModel.clear();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        final String extensionFinal = ext.toLowerCase();

        // Ejecutar la búsqueda en un hilo secundario para no congelar la GUI Swing
        new Thread(() -> {
            File[] roots = File.listRoots(); // Obtener todas las unidades de disco
            if (roots != null) {
                for (File root : roots) {
                    buscarEnDirectorio(root, extensionFinal);
                }
            }
        }).start();
    }

    /**
     * Recorre de forma recursiva carpetas y subcarpetas para encontrar ficheros.
     */
    private void buscarEnDirectorio(File dir, String ext) {
        File[] ficheros = dir.listFiles();
        if (ficheros == null) return; // Si no hay acceso o permisos, omitir

        for (File f : ficheros) {
            if (f.isDirectory()) {
                buscarEnDirectorio(f, ext);
            } else if (f.isFile() && f.getName().toLowerCase().endsWith(ext)) {
                // Formato exigido: <Unidad>:\<ruta>\<nombre>.<ext>
                String rutaAbsoluta = f.getAbsolutePath();
                SwingUtilities.invokeLater(() -> listModel.addElement(rutaAbsoluta));
            }
        }
    }

    /**
     * Ejecuta el archivo si termina en .exe al hacer doble clic usando Runtime/ProcessBuilder.
     */
    private void ejecutarSeleccionado() {
        String rutaFichero = listResultados.getSelectedValue();
        if (rutaFichero == null) return;

        if (rutaFichero.toLowerCase().endsWith(".exe")) {
            try {
                // Creación de proceso mediante la clase Runtime estudiada en el Tema 1
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(rutaFichero);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al lanzar el ejecutable: " + ex.getMessage(),
                                              "Error de Procesos", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Solo se pueden ejecutar ficheros con extensión .exe", 
                                          "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new proyecto().setVisible(true));
    }
}