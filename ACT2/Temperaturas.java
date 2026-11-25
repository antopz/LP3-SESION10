import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class Temperaturas extends JFrame implements ActionListener {
    private String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private JTextField[] campos = new JTextField[7];
    private JButton boton = new JButton("Mostrar Gráfico");
    private GraficoTemperaturas grafico = new GraficoTemperaturas();
    public Temperaturas() {
        setTitle("Temperaturas de la semana");
        setSize(850, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        JPanel parteSuperior = new JPanel(new BorderLayout(10, 10));
        JPanel entradas = new JPanel(new GridLayout(2, 7, 8, 6));
        for (int i = 0; i < dias.length; i++) {
            entradas.add(new JLabel(dias[i], SwingConstants.CENTER));
        }
        for (int i = 0; i < campos.length; i++) {
            campos[i] = new JTextField();
            entradas.add(campos[i]);
        }
        parteSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        parteSuperior.add(entradas, BorderLayout.CENTER);
        parteSuperior.add(boton, BorderLayout.SOUTH);
        add(parteSuperior, BorderLayout.NORTH);
        add(grafico, BorderLayout.CENTER);
        boton.addActionListener(this);
        setVisible(true);
    }
    public void actionPerformed(ActionEvent e) {
        double[] temperaturas = new double[7];
        for (int i = 0; i < campos.length; i++) {
            String texto = campos[i].getText().trim();
            if (texto.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa la temperatura del " + dias[i] + ".");
                return;
            }
            try {
                temperaturas[i] = Double.parseDouble(texto.replace(',', '.'));
                if (Double.isNaN(temperaturas[i]) || Double.isInfinite(temperaturas[i])) {
                    JOptionPane.showMessageDialog(this, "Ingresa una temperatura válida para el " + dias[i] + ".");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "La temperatura del " + dias[i] + " debe ser un número.");
                return;
            }
        }
        grafico.setTemperaturas(temperaturas);
    }
    public static void main(String[] args) {
        new Temperaturas();
    }
}
