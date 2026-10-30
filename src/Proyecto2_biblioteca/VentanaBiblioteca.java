package Proyecto2_biblioteca;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class VentanaBiblioteca extends JFrame {
    private JPanel panelPrincipal;
    private JTable tablaCatalogo;
    private JTextField txtBuscarId;
    private JButton btnBuscar;
    private JButton btnPrestar;
    private JButton btnDevolver;
    private JList<String> listRecomendaciones;
    private JList<String> listCola;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtCategoria;
    private JTextField txtStock;
    private JButton btnRegistrar;

    private ArbolAVL arbolAVL = new ArbolAVL();
    private Map<Integer, List<Integer>> grafoAdyacencia = new HashMap<>();
    private Map<Integer, Libro> mapaLibros = new HashMap<>();

    private DefaultTableModel modeloTabla;
    private DefaultListModel<String> modeloRec = new DefaultListModel<>();
    private DefaultListModel<String> modeloCola = new DefaultListModel<>();

    public VentanaBiblioteca() {
        setTitle("Sistema de Gestión de Biblioteca - Proyecto 2");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "Categoría", "Stock"}, 0);
        if (tablaCatalogo != null) tablaCatalogo.setModel(modeloTabla);
        if (listRecomendaciones != null) listRecomendaciones.setModel(modeloRec);
        if (listCola != null) listCola.setModel(modeloCola);

        cargarDatosCSV("src/Proyecto2_biblioteca/libros.csv");
        actualizarTabla();

        if (btnBuscar != null) btnBuscar.addActionListener(e -> buscar());
        if (btnPrestar != null) btnPrestar.addActionListener(e -> prestar());
        if (btnDevolver != null) btnDevolver.addActionListener(e -> devolver());
        if (btnRegistrar != null) btnRegistrar.addActionListener(e -> registrar());

        if (tablaCatalogo != null) {
            tablaCatalogo.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) seleccionarLibro();
            });
        }

        pack();
        setLocationRelativeTo(null);
    }

    private void registrarLibroEnEstructuras(Libro l) {
        arbolAVL.insertar(l);
        mapaLibros.put(l.getId(), l);
        grafoAdyacencia.putIfAbsent(l.getId(), new ArrayList<>());

        for (Libro exist : mapaLibros.values()) {
            if (exist.getId() != l.getId() &&
                    (exist.getCategoria().equalsIgnoreCase(l.getCategoria()) || exist.getAutor().equalsIgnoreCase(l.getAutor()))) {
                grafoAdyacencia.get(l.getId()).add(exist.getId());
                grafoAdyacencia.get(exist.getId()).add(l.getId());
            }
        }
    }

    private void cargarDatosCSV(String ruta) {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 5) {
                    Libro l = new Libro(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim(), Integer.parseInt(p[4].trim()));
                    registrarLibroEnEstructuras(l);
                }
            }
        } catch (Exception e) {
            System.out.println("Cargando datos iniciales...");
        }
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Libro l : arbolAVL.obtenerInorden()) {
            modeloTabla.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getStock()});
        }
    }

    private void seleccionarLibro() {
        int row = tablaCatalogo.getSelectedRow(); // Obtiene la fila seleccionada en la JTable
        if (row >= 0) { // Verifica que haya una fila seleccionada válidamente
            int id = (int) modeloTabla.getValueAt(row, 0); // Extrae el ID del libro (columna 0)
            Libro l = arbolAVL.buscar(id); // Busca el libro en el Árbol AVL usando su ID
            if (l != null) { // Si el libro existe en el catálogo
                modeloRec.clear(); // Limpia la lista visual de recomendaciones
                Set<Integer> vis = new HashSet<>(); // Conjunto para no repetir nodos visitados en BFS
                Queue<Integer> q = new LinkedList<>(); // Cola FIFO para procesar el recorrido BFS
                q.add(id); vis.add(id); // Añade el nodo inicial a la cola y lo marca visitado

                while (!q.isEmpty() && modeloRec.size() < 5) { // Bucle BFS (límite de 5 recomendaciones)
                    int curr = q.poll(); // Extrae el ID del libro actual de la cola
                    for (int adj : grafoAdyacencia.getOrDefault(curr, new ArrayList<>())) { // Recorre sus vecinos afines
                        if (!vis.contains(adj)) { // Si el vecino no ha sido visitado
                            vis.add(adj); // Marca el libro vecino como visitado
                            q.add(adj); // Lo agrega a la cola para explorar sus vecinos
                            modeloRec.addElement(mapaLibros.get(adj).getTitulo()); // Agrega el título a la GUI
                        }
                    }
                }

                modeloCola.clear(); // Limpia la lista visual de la cola de espera
                for (String u : l.getColaEspera()) modeloCola.addElement(u); // Muestra los estudiantes en espera
            }
        }
    }

    private void buscar() {
        try {
            Libro l = arbolAVL.buscar(Integer.parseInt(txtBuscarId.getText().trim()));
            JOptionPane.showMessageDialog(this, l != null ? l.toString() : "Libro no encontrado.");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ingrese un ID válido."); }
    }

    private void prestar() {
        int row = tablaCatalogo.getSelectedRow();
        if (row >= 0) {
            Libro l = arbolAVL.buscar((int) modeloTabla.getValueAt(row, 0));
            if (!l.prestar("")) {
                String nom = JOptionPane.showInputDialog(this, "Libro AGOTADO. Ingrese nombre para la COLA DE ESPERA:");
                if (nom != null && !nom.trim().isEmpty()) l.getColaEspera().add(nom);
            }
            actualizarTabla();
            seleccionarLibro();
        }
    }

    private void devolver() {
        int row = tablaCatalogo.getSelectedRow();
        if (row >= 0) {
            Libro l = arbolAVL.buscar((int) modeloTabla.getValueAt(row, 0));
            String sig = l.devolver();
            JOptionPane.showMessageDialog(this, sig != null ? "Asignado a: " + sig : "Devuelto al stock.");
            actualizarTabla();
            seleccionarLibro();
        }
    }

    private void registrar() {
        try {
            int id = mapaLibros.size() + 1001;
            Libro l = new Libro(id, txtTitulo.getText().trim(), txtAutor.getText().trim(), txtCategoria.getText().trim(), Integer.parseInt(txtStock.getText().trim()));
            registrarLibroEnEstructuras(l);
            actualizarTabla();
            txtTitulo.setText(""); txtAutor.setText(""); txtCategoria.setText(""); txtStock.setText("");
            JOptionPane.showMessageDialog(this, "Registrado con ID: " + id);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Verifique los campos."); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaBiblioteca().setVisible(true));
    }
}