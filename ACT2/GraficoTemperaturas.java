import javax.swing.*;
import java.awt.*;
public class GraficoTemperaturas extends JPanel {
    private double[] temperaturas;
    private String[] dias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    public void setTemperaturas(double[] temperaturas) {
        this.temperaturas = temperaturas;
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (temperaturas == null) {
            g.drawString("Ingresa las temperaturas y presiona Mostrar Gráfico.", 35, 45);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int izquierda = 55;
        int derecha = getWidth() - 35;
        int arriba = 55;
        int abajo = getHeight() - 55;
        int ancho = derecha - izquierda;
        int alto = abajo - arriba;
        double minimo = temperaturas[0];
        double maximo = temperaturas[0];
        for (int i = 1; i < temperaturas.length; i++) {
            if (temperaturas[i] < minimo) minimo = temperaturas[i];
            if (temperaturas[i] > maximo) maximo = temperaturas[i];
        }
        if (minimo == maximo) {
            minimo = minimo - 5;
            maximo = maximo + 5;
        } else {
            minimo = minimo - 2;
            maximo = maximo + 2;
        }
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 4; i++) {
            int y = arriba + i * alto / 4;
            g.drawLine(izquierda, y, derecha, y);
            double valor = maximo - i * (maximo - minimo) / 4;
            g.setColor(Color.BLACK);
            g.drawString(String.format("%.1f", valor), 5, y + 4);
            g.setColor(Color.LIGHT_GRAY);
        }
        g.setColor(Color.BLACK);
        g.drawLine(izquierda, arriba, izquierda, abajo);
        g.drawLine(izquierda, abajo, derecha, abajo);
        g.drawString("Temperatura (°C)", izquierda, 25);
        int anteriorX = 0;
        int anteriorY = 0;
        for (int i = 0; i < temperaturas.length; i++) {
            int x = izquierda + i * ancho / 6;
            int y = abajo - (int) ((temperaturas[i] - minimo) / (maximo - minimo) * alto);
            if (i > 0) {
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(anteriorX, anteriorY, x, y);
            }
            g2.setColor(Color.RED);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.setColor(Color.BLACK);
            g2.drawString(dias[i], x - 12, abajo + 20);
            g2.drawString(String.format("%.1f", temperaturas[i]), x - 12, y - 12);
            anteriorX = x;
            anteriorY = y;
        }
    }
}
