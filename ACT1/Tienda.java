import javax.swing.*;
import java.awt.event.*;
public class Tienda extends JFrame implements ActionListener {
    private JTextField campoNombre = new JTextField();
    private JTextField campoPrecio = new JTextField();
    private JTextField campoStock = new JTextField();
    private JTextField campoCategoria = new JTextField();
    private JButton boton = new JButton("Actualizar Producto");
    private JLabel resultado = new JLabel("Ingresa los datos del producto.");
    private Producto producto = new Producto("Sin registrar", 0, 0, "Sin categoría");
    public Tienda() {
        setTitle("Registro de productos");
        setSize(520, 410);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        JLabel nombre = new JLabel("Nombre:");
        JLabel precio = new JLabel("Precio (S/):");
        JLabel stock = new JLabel("Stock:");
        JLabel categoria = new JLabel("Categoría:");
        nombre.setBounds(35, 25, 120, 25);
        campoNombre.setBounds(160, 25, 290, 25);
        precio.setBounds(35, 65, 120, 25);
        campoPrecio.setBounds(160, 65, 290, 25);
        stock.setBounds(35, 105, 120, 25);
        campoStock.setBounds(160, 105, 290, 25);
        categoria.setBounds(35, 145, 120, 25);
        campoCategoria.setBounds(160, 145, 290, 25);
        boton.setBounds(160, 195, 220, 35);
        resultado.setBounds(35, 240, 440, 110);
        add(nombre);
        add(campoNombre);
        add(precio);
        add(campoPrecio);
        add(stock);
        add(campoStock);
        add(categoria);
        add(campoCategoria);
        add(boton);
        add(resultado);
        boton.addActionListener(this);
        setVisible(true);
    }
    public void actionPerformed(ActionEvent e) {
        try {
            String nombre = campoNombre.getText().trim();
            String categoria = campoCategoria.getText().trim();
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("Ingresa el nombre del producto.");
            }
            if (campoPrecio.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Ingresa el precio del producto.");
            }
            if (campoStock.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Ingresa la cantidad en stock.");
            }
            if (categoria.isEmpty()) {
                throw new IllegalArgumentException("Ingresa la categoría del producto.");
            }
            double precio;
            int stock;
            try {
                precio = Double.parseDouble(campoPrecio.getText().trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("El precio debe ser un número. Ejemplo: 15.50.");
            }
            try {
                stock = Integer.parseInt(campoStock.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("El stock debe ser un número entero.");
            }
            if (Double.isNaN(precio) || Double.isInfinite(precio)) {
                throw new IllegalArgumentException("Ingresa un precio válido.");
            }
            if (precio < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            if (stock < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo.");
            }
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setCantidadStock(stock);
            producto.setCategoria(categoria);
            resultado.setText("<html><b>Producto actualizado correctamente</b><br>Nombre: " + producto.getNombre() + "<br>Precio: S/ " + String.format("%.2f", producto.getPrecio()) + "<br>Stock: " + producto.getCantidadStock() + " unidades<br>Categoría: " + producto.getCategoria() + "</html>");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        }
    }
    public static void main(String[] args) {
        new Tienda();
    }
}
