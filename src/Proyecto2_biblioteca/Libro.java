package Proyecto2_biblioteca;

import java.util.LinkedList;
import java.util.Queue;

public class Libro {
    private int id;
    private String titulo;
    private String autor;
    private String categoria;
    private int stock;
    private Queue<String> colaEspera;

    public Libro(int id, String titulo, String autor, String categoria, int stock) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.stock = stock;
        this.colaEspera = new LinkedList<>();
    }

    // BLOQUE FIFO PRINCIPAL
    public boolean prestar(String usuario) {
        if (stock > 0) { // Si hay ejemplares físicos disponibles
            stock--; // Descuenta un ejemplar
            return true; // Préstamo otorgado de manera inmediata
        } else { // Si el libro está agotado (stock == 0)[cite: 1, 2]
            colaEspera.add(usuario); // Encola al estudiante al final de la Queue FIFO[cite: 1, 2]
            return false; // Indica que se registró en lista de espera[cite: 1, 2]
        }
    }

    public String devolver() {
        if (!colaEspera.isEmpty()) { // Si existen usuarios pendientes en la lista de espera[cite: 1, 2]
            return colaEspera.poll(); // Extrae y asigna al primer estudiante registrado (FIFO)[cite: 1, 2]
        } else { // Si no hay nadie en espera[cite: 1, 2]
            stock++; // Reintegra el ejemplar físico incrementando el stock[cite: 1, 2]
            return null; // Retorna a estantería general[cite: 1, 2]
        }
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getCategoria() { return categoria; }
    public int getStock() { return stock; }
    public Queue<String> getColaEspera() { return colaEspera; }

    @Override
    public String toString() {
        return "[" + id + "] " + titulo + " - " + autor + " (" + categoria + ") | Stock: " + stock;
    }
}