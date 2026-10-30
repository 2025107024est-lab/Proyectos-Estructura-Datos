package Proyecto1_gestor_tareas;

public class Tarea {
    private int id;
    private String titulo;
    private String descripcion;
    private int prioridad; // 1: Alta, 2: Media, 3: Baja
    private String estado;  // "POR_HACER", "EN_PROCESO", "FINALIZADO"

    public Tarea(int id, String titulo, String descripcion, int prioridad, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public int getPrioridad() { return prioridad; }
    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        String pTexto = (prioridad == 1) ? "Alta" : (prioridad == 2) ? "Media" : "Baja";
        return "[" + pTexto + "] " + titulo;
    }
}