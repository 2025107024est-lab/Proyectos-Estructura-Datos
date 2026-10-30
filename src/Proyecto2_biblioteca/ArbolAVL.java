package Proyecto2_biblioteca;

import java.util.ArrayList;
import java.util.List;

public class ArbolAVL {
    private class Nodo {
        Libro libro;
        Nodo izquierdo, derecho;
        int altura = 1;
        Nodo(Libro libro) { this.libro = libro; }
    }

    private Nodo raiz;

    private int altura(Nodo n) { return n == null ? 0 : n.altura; }
    private int balance(Nodo n) { return n == null ? 0 : altura(n.izquierdo) - altura(n.derecho); }

    private Nodo rotDerecha(Nodo y) {
        Nodo x = y.izquierdo;
        Nodo T2 = x.derecho;
        x.derecho = y;
        y.izquierdo = T2;
        y.altura = Math.max(altura(y.izquierdo), altura(y.derecho)) + 1;
        x.altura = Math.max(altura(x.izquierdo), altura(x.derecho)) + 1;
        return x;
    }

    private Nodo rotIzquierda(Nodo x) {
        Nodo y = x.derecho;
        Nodo T2 = y.izquierdo;
        y.izquierdo = x;
        x.derecho = T2;
        x.altura = Math.max(altura(x.izquierdo), altura(x.derecho)) + 1;
        y.altura = Math.max(altura(y.izquierdo), altura(y.derecho)) + 1;
        return y;
    }

    public void insertar(Libro libro) { raiz = insertarRec(raiz, libro); }

    private Nodo insertarRec(Nodo nodo, Libro libro) {
        if (nodo == null) return new Nodo(libro);
        if (libro.getId() < nodo.libro.getId()) nodo.izquierdo = insertarRec(nodo.izquierdo, libro);
        else if (libro.getId() > nodo.libro.getId()) nodo.derecho = insertarRec(nodo.derecho, libro);
        else return nodo;

        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
        int b = balance(nodo);

        if (b > 1 && libro.getId() < nodo.izquierdo.libro.getId()) return rotDerecha(nodo);
        if (b < -1 && libro.getId() > nodo.derecho.libro.getId()) return rotIzquierda(nodo);
        if (b > 1 && libro.getId() > nodo.izquierdo.libro.getId()) {
            nodo.izquierdo = rotIzquierda(nodo.izquierdo);
            return rotDerecha(nodo);
        }
        if (b < -1 && libro.getId() < nodo.derecho.libro.getId()) {
            nodo.derecho = rotDerecha(nodo.derecho);
            return rotIzquierda(nodo);
        }
        return nodo;
    }

    public Libro buscar(int id) {
        return buscarRec(raiz, id); // Inicia la búsqueda recursiva desde la raíz
    }

    private Libro buscarRec(Nodo n, int id) {
        if (n == null) return null; // Retorna null si el libro no existe en el catálogo
        if (id == n.libro.getId()) return n.libro; // Retorna el objeto si coincide el ID
        return (id < n.libro.getId()) ? buscarRec(n.izquierdo, id) : buscarRec(n.derecho, id); // Navega recursivamente
    }

    // 2. Recorrido Inorden para extraer el catálogo completamente ordenado
    public List<Libro> obtenerInorden() {
        List<Libro> lista = new ArrayList<>(); // Lista temporal receptora
        inorden(raiz, lista); // Ejecuta el recorrido inorden desde la raíz
        return lista; // Retorna la lista ordenada ascendentemente por ID
    }

    private void inorden(Nodo n, List<Libro> l) {
        if (n != null) {
            inorden(n.izquierdo, l); // Visita el subárbol izquierdo
            l.add(n.libro);          // Visita el nodo central (añade el libro)
            inorden(n.derecho, l);   // Visita el subárbol derecho
        }
    }
}
