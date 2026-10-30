package Proyecto1_gestor_tareas;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class VentanaGestor {
    // Componentes enlazados con el diseñador (.form)
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JTextField txtTitulo;
    private JTextField txtDescripcion;
    private JComboBox<String> cbPrioridad;
    private JButton btnAgregar;
    private JButton btnEliminar;
    private JButton btnDeshacer;
    private JPanel panelTablero;
    private JButton btnMoverAProceso;
    private JButton btnMoverAFinalizado;

    private JList<Tarea> listaPorHacer;
    private JList<Tarea> listaEnProceso;
    private JList<Tarea> listaFinalizado;

    // Modelos para las listas
    private DefaultListModel<Tarea> modeloPorHacer;
    private DefaultListModel<Tarea> modeloEnProceso;
    private DefaultListModel<Tarea> modeloFinalizado;

    // Pila de objetos Accion para restaurar tareas (Es como un Ctrl + Z)
    private Stack<Accion> pilaHistorial;
    private int contadorId = 1;

    // Clase simple para registrar que se hizo en la Pila
    private static class Accion {
        String tipo; // "AGREGAR", "ELIMINAR", "MOVER"
        Tarea tarea;
        DefaultListModel<Tarea> modeloOrigen;
        DefaultListModel<Tarea> modeloDestino;

        public Accion(String tipo, Tarea tarea, DefaultListModel<Tarea> modeloOrigen, DefaultListModel<Tarea> modeloDestino) {
            this.tipo = tipo;
            this.tarea = tarea;
            this.modeloOrigen = modeloOrigen;
            this.modeloDestino = modeloDestino;
        }
    }

    public VentanaGestor() {
        pilaHistorial = new Stack<>();

        // Inicializar modelos
        modeloPorHacer = new DefaultListModel<>();
        modeloEnProceso = new DefaultListModel<>();
        modeloFinalizado = new DefaultListModel<>();

        if (listaPorHacer != null) listaPorHacer.setModel(modeloPorHacer);
        if (listaEnProceso != null) listaEnProceso.setModel(modeloEnProceso);
        if (listaFinalizado != null) listaFinalizado.setModel(modeloFinalizado);

        if (cbPrioridad != null) {
            cbPrioridad.removeAllItems();
            cbPrioridad.addItem("1 - Alta");
            cbPrioridad.addItem("2 - Media");
            cbPrioridad.addItem("3 - Baja");
        }

        // Eventos
        if (btnAgregar != null) btnAgregar.addActionListener(e -> agregarTarea());
        if (btnEliminar != null) btnEliminar.addActionListener(e -> eliminarTareaSeleccionada());
        if (btnDeshacer != null) btnDeshacer.addActionListener(e -> deshacerAccion());

        if (btnMoverAProceso != null) {
            btnMoverAProceso.addActionListener(e -> moverTarea(listaPorHacer, modeloPorHacer, modeloEnProceso, "EN_PROCESO"));
        }
        if (btnMoverAFinalizado != null) {
            btnMoverAFinalizado.addActionListener(e -> moverTarea(listaEnProceso, modeloEnProceso, modeloFinalizado, "FINALIZADO"));
        }

        // Estilos limpios y normales
        aplicarEstilosVisuales();

        cargarDatosPrueba();
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

    // --- FUNCIÓN DE ORDENAMIENTO POR PRIORIDAD (1: Alta arriba -> 3: Baja abajo)
    private void reordenarModeloPorPrioridad(DefaultListModel<Tarea> modelo) {
        List<Tarea> listaTemp = new ArrayList<>(); // Crea la lista temporal
        for (int i = 0; i < modelo.getSize(); i++) { // Bucle para recorrer la lista visual
            listaTemp.add(modelo.getElementAt(i)); // Copia cada tarea a la lista temporal
        }

        // Comparador de prioridad
        Collections.sort(listaTemp, (t1, t2) -> Integer.compare(t1.getPrioridad(), t2.getPrioridad())); // Ordena

        modelo.clear(); // Limpia el modelo de la interfaz
        for (Tarea t : listaTemp) { // Bucle para recorrer las tareas ordenadas
            modelo.addElement(t); // Vuelve a insertar las tareas ya ordenadas
        }
    }

    private void agregarTarea() {
        String titulo = txtTitulo.getText().trim();
        String desc = txtDescripcion.getText().trim();
        int prioridad = cbPrioridad.getSelectedIndex() + 1;

        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Ingresa un título para la tarea.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Tarea nueva = new Tarea(contadorId++, titulo, desc, prioridad, "POR_HACER");
        modeloPorHacer.addElement(nueva);
        reordenarModeloPorPrioridad(modeloPorHacer); // <-- ORDENA AUTOMÁTICAMENTE

        // Guardar en la Pila para poder deshacer la creacion
        pilaHistorial.push(new Accion("AGREGAR", nueva, modeloPorHacer, null));

        txtTitulo.setText("");
        txtDescripcion.setText("");
    }

    private void eliminarTareaSeleccionada() {
        if (listaPorHacer != null && !listaPorHacer.isSelectionEmpty()) {
            Tarea t = listaPorHacer.getSelectedValue();
            modeloPorHacer.removeElement(t);
            pilaHistorial.push(new Accion("ELIMINAR", t, modeloPorHacer, null));
        } else if (listaEnProceso != null && !listaEnProceso.isSelectionEmpty()) {
            Tarea t = listaEnProceso.getSelectedValue();
            modeloEnProceso.removeElement(t);
            pilaHistorial.push(new Accion("ELIMINAR", t, modeloEnProceso, null));
        } else if (listaFinalizado != null && !listaFinalizado.isSelectionEmpty()) {
            Tarea t = listaFinalizado.getSelectedValue();
            modeloFinalizado.removeElement(t);
            pilaHistorial.push(new Accion("ELIMINAR", t, modeloFinalizado, null));
        } else {
            JOptionPane.showMessageDialog(panelPrincipal, "Selecciona una tarea para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void moverTarea(JList<Tarea> origenList, DefaultListModel<Tarea> origenModel, DefaultListModel<Tarea> destinoModel, String nuevoEstado) {
        if (origenList == null || origenList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Selecciona una tarea para moverla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Tarea t = origenList.getSelectedValue();
        origenModel.removeElement(t);
        t.setEstado(nuevoEstado);
        destinoModel.addElement(t);
        reordenarModeloPorPrioridad(destinoModel); // <-- ORDENA LA COLUMNA DE DESTINO AUTOMÁTICAMENTE

        // Guardar el origen y el destino para regresarla si se hace clic en Deshacer
        pilaHistorial.push(new Accion("MOVER", t, origenModel, destinoModel));
    }

    private void deshacerAccion() {
        if (pilaHistorial.isEmpty()) { // Si la pila no tiene registros
            JOptionPane.showMessageDialog(panelPrincipal, "La Pila está vacía.", "Pila Vacía", JOptionPane.INFORMATION_MESSAGE); // Muestra mensaje de aviso
            return; // Cancela la ejecución del método
        }

        // Desapilar (LIFO)
        Accion ultima = pilaHistorial.pop(); // Extrae el último movimiento guardado

        if (ultima.tipo.equals("AGREGAR")) { // Si la última acción fue crear una tarea
            ultima.modeloOrigen.removeElement(ultima.tarea); // Elimina la tarea agregada
        } else if (ultima.tipo.equals("ELIMINAR")) { // Si la última acción fue borrar
            ultima.modeloOrigen.addElement(ultima.tarea); // Restaura la tarea en la lista origen
            reordenarModeloPorPrioridad(ultima.modeloOrigen); // Reordena la columna por prioridad
        } else if (ultima.tipo.equals("MOVER")) { // Si la última acción fue mover entre columnas
            ultima.modeloDestino.removeElement(ultima.tarea); // Quita la tarea de la columna destino
            ultima.modeloOrigen.addElement(ultima.tarea); // Regresa la tarea a la columna origen
            reordenarModeloPorPrioridad(ultima.modeloOrigen); // Reordena la columna por prioridad
        }
    }

    private void aplicarEstilosVisuales() {
        // Fondos limpios tipo papel / libreta
        if (listaPorHacer != null) {
            listaPorHacer.setBackground(new Color(248, 249, 250));
            listaPorHacer.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 205, 210)), "POR HACER",
                    TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), Color.DARK_GRAY));
        }
        if (listaEnProceso != null) {
            listaEnProceso.setBackground(new Color(255, 253, 245));
            listaEnProceso.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(230, 215, 170)), "EN PROCESO",
                    TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), Color.DARK_GRAY));
        }
        if (listaFinalizado != null) {
            listaFinalizado.setBackground(new Color(245, 250, 245));
            listaFinalizado.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(190, 220, 190)), "FINALIZADO",
                    TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), Color.DARK_GRAY));
        }
    }

    private void cargarDatosPrueba() {
        modeloPorHacer.addElement(new Tarea(contadorId++, "Probar botón Eliminar", "Eliminar una tarea seleccionada", 2, "POR_HACER"));
        modeloPorHacer.addElement(new Tarea(contadorId++, "Diseñar GUI Form", "Arrastrar componentes en IntelliJ", 1, "POR_HACER"));
        reordenarModeloPorPrioridad(modeloPorHacer); // Carga ordenada
    }
}