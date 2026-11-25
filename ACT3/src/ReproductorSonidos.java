import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class ReproductorSonidos extends JFrame {

    private Clip clipActual;

    public ReproductorSonidos() {
        setTitle("Reproductor de efectos de sonido");
        setSize(480, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("REPRODUCTOR DE SONIDOS", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));

        agregarBoton(panel, "Aplausos", "aplausos.wav");
        agregarBoton(panel, "Campana", "campana.wav");
        agregarBoton(panel, "Explosión", "explosion.wav");
        agregarBoton(panel, "Láser", "laser.wav");
        agregarBoton(panel, "Sirena", "sirena.wav");
        agregarBoton(panel, "Lluvia", "lluvia.wav");
        agregarBoton(panel, "Teléfono", "telefono.wav");
        agregarBoton(panel, "Éxito", "exito.wav");

        add(panel, BorderLayout.CENTER);

        JButton detener = new JButton("Detener sonido");
        detener.addActionListener(e -> detenerSonido());
        add(detener, BorderLayout.SOUTH);
    }

    private void agregarBoton(JPanel panel, String nombre, String archivo) {
        JButton boton = new JButton(nombre);
        boton.setFont(new Font("Arial", Font.PLAIN, 15));
        boton.addActionListener(e -> reproducirSonido(archivo));
        panel.add(boton);
    }

    private void reproducirSonido(String archivo) {
        detenerSonido();
        try {
            URL ruta = getClass().getResource("/sonidos/" + archivo);
            if (ruta == null) {
                throw new Exception("No se encontró " + archivo);
            }
            try (AudioInputStream audio = AudioSystem.getAudioInputStream(ruta)) {
                clipActual = AudioSystem.getClip();
                clipActual.open(audio);
                clipActual.start();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo reproducir: " + ex.getMessage());
        }
    }

    private void detenerSonido() {
        if (clipActual != null) {
            clipActual.stop();
            clipActual.close();
            clipActual = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReproductorSonidos().setVisible(true));
    }
}
