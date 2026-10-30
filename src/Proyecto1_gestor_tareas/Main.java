package Proyecto1_gestor_tareas;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gestor de Tareas y Proyectos Personales");
            VentanaGestor gestor = new VentanaGestor();

            frame.setContentPane(gestor.getPanelPrincipal());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setSize(950, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}