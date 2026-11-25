public class Producto {
    private String nombre;
    private double precio;
    private int cantidadStock;
    private String categoria;
    public Producto(String nombre, double precio, int cantidadStock, String categoria) {
        setNombre(nombre);
        setPrecio(precio);
        setCantidadStock(cantidadStock);
        setCategoria(categoria);
    }
    public String getNombre() {
        return nombre;
    }
    public double getPrecio() {
        return precio;
    }
    public int getCantidadStock() {
        return cantidadStock;
    }
    public String getCategoria() {
        return categoria;
    }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa el nombre del producto.");
        }
        this.nombre = nombre.trim();
    }
    public void setPrecio(double precio) {
        if (Double.isNaN(precio) || Double.isInfinite(precio)) {
            throw new IllegalArgumentException("Ingresa un precio válido.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }
    public void setCantidadStock(int cantidadStock) {
        if (cantidadStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.cantidadStock = cantidadStock;
    }
    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa la categoría del producto.");
        }
        this.categoria = categoria.trim();
    }
}
