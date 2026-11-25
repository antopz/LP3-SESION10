package ACT;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ReproductorMusical extends JFrame {
    private Clip clip;
    private File audioFile;
    private boolean paused = false;
    private long pausePosition = 0;

    private JLabel statusLabel, songLabel, elapsedLabel, totalLabel;
    private JProgressBar progressBar;
    private JButton playButton, pauseButton, resumeButton, chooseButton;
    private Timer uiTimer;

    private final Color BG = new Color(13, 16, 28);
    private final Color PANEL = new Color(24, 29, 47);
    private final Color PURPLE = new Color(139, 92, 246);
    private final Color CYAN = new Color(34, 211, 238);
    private final Color TEXT = new Color(238, 242, 255);
    private final Color MUTED = new Color(155, 163, 186);

    public ReproductorMusical() {
        setTitle("Pulse Player");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(780, 560);
        setMinimumSize(new Dimension(700, 520));
        setLocationRelativeTo(null);
        setContentPane(new GradientPanel());
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 30, 24, 30));
        add(root);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        JLabel icon = new JLabel("♫");
        icon.setFont(new Font("SansSerif", Font.BOLD, 30));
        icon.setForeground(CYAN);
        JLabel title = new JLabel("PULSE");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("MUSIC PLAYER");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        subtitle.setForeground(MUTED);
        brand.add(icon);
        brand.add(title);
        brand.add(subtitle);
        header.add(brand, BorderLayout.WEST);

        JLabel live = new JLabel("●  DESKTOP AUDIO");
        live.setForeground(new Color(110, 231, 183));
        live.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.add(live, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Main card
        JPanel card = new RoundedPanel(28, PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        root.add(card, BorderLayout.CENTER);

        // Artwork
        JPanel artworkWrap = new JPanel(new GridBagLayout());
        artworkWrap.setOpaque(false);
        artworkWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        artworkWrap.setPreferredSize(new Dimension(210, 180));
        artworkWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel artwork = new RoundedPanel(26, new Color(40, 35, 75));
        artwork.setPreferredSize(new Dimension(170, 160));
        artwork.setLayout(new GridBagLayout());
        JLabel musicSymbol = new JLabel("♫");
        musicSymbol.setFont(new Font("SansSerif", Font.PLAIN, 88));
        musicSymbol.setForeground(new Color(220, 210, 255));
        artwork.add(musicSymbol);
        artworkWrap.add(artwork);
        card.add(artworkWrap);

        songLabel = new JLabel("Ninguna canción seleccionada");
        songLabel.setForeground(TEXT);
        songLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        songLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(songLabel);

        JLabel artistLabel = new JLabel("Tu música, tu momento");
        artistLabel.setForeground(MUTED);
        artistLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(6));
        card.add(artistLabel);
        card.add(Box.createVerticalStrut(22));

        // Progress
        JPanel progressPanel = new JPanel(new BorderLayout(10, 5));
        progressPanel.setOpaque(false);
        elapsedLabel = new JLabel("00:00");
        totalLabel = new JLabel("00:00");
        elapsedLabel.setForeground(MUTED);
        totalLabel.setForeground(MUTED);
        elapsedLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        totalLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 1000);
        progressBar.setValue(0);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(55, 61, 82));
        progressBar.setForeground(CYAN);
        progressBar.setPreferredSize(new Dimension(100, 8));

        progressPanel.add(elapsedLabel, BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(totalLabel, BorderLayout.EAST);
        card.add(progressPanel);
        card.add(Box.createVerticalStrut(22));

        // Playback buttons
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        controls.setOpaque(false);

        playButton = makeButton("▶  REPRODUCIR", PURPLE, Color.WHITE);
        pauseButton = makeButton("Ⅱ  PAUSAR", new Color(45, 54, 78), TEXT);
        resumeButton = makeButton("▶  REANUDAR", new Color(20, 125, 145), Color.WHITE);

        playButton.addActionListener(e -> playFromStart());
        pauseButton.addActionListener(e -> pauseAudio());
        resumeButton.addActionListener(e -> resumeAudio());

        controls.add(playButton);
        controls.add(pauseButton);
        controls.add(resumeButton);
        card.add(controls);
        card.add(Box.createVerticalStrut(18));

        statusLabel = new JLabel("Selecciona un archivo de audio para comenzar");
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);

        // Bottom file picker
        chooseButton = makeButton("＋  ABRIR ARCHIVO DE AUDIO", new Color(35, 41, 61), TEXT);
        chooseButton.addActionListener(e -> chooseAudio());
        chooseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(20));
        card.add(chooseButton);

        uiTimer = new Timer(250, e -> updateProgress());
    }

    private JButton makeButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(14, 18, 14, 18));
        button.setPreferredSize(new Dimension(190, 46));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(background.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }
        });
        return button;
    }

    private void chooseAudio() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo de audio");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Audio compatible (WAV, AIFF, AU)", "wav", "aiff", "au"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            audioFile = chooser.getSelectedFile();
            songLabel.setText(shorten(audioFile.getName(), 38));
            statusLabel.setText("Archivo cargado · listo para reproducir");
            loadClip();
        }
    }

    private String shorten(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }

    private void loadClip() {
        closeClip();
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile)) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            pausePosition = 0;
            paused = false;
            totalLabel.setText(formatTime(clip.getMicrosecondLength()));
            progressBar.setValue(0);
            elapsedLabel.setText("00:00");
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el audio.\nUsa un archivo WAV, AIFF o AU compatible.\n\n" + ex.getMessage(),
                    "Error de audio", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("No se pudo cargar el archivo");
        }
    }

    private void playFromStart() {
        if (audioFile == null) {
            chooseAudio();
            if (audioFile == null) return;
        }
        if (clip == null || !clip.isOpen()) loadClip();
        if (clip == null) return;

        clip.stop();
        clip.setMicrosecondPosition(0);
        pausePosition = 0;
        paused = false;
        clip.start();
        uiTimer.start();
        statusLabel.setText("● Reproduciendo ahora");
    }

    private void pauseAudio() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition();
            clip.stop();
            paused = true;
            uiTimer.stop();
            updateProgress();
            statusLabel.setText("Ⅱ Audio pausado");
        } else {
            statusLabel.setText("La pista no está reproduciéndose");
        }
    }

    private void resumeAudio() {
        if (clip != null && paused) {
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
            paused = false;
            uiTimer.start();
            statusLabel.setText("● Reproducción reanudada");
        } else if (clip == null) {
            statusLabel.setText("Primero selecciona y reproduce una canción");
        } else {
            statusLabel.setText("El audio no está pausado");
        }
    }

    private void updateProgress() {
        if (clip == null) return;
        long current = clip.getMicrosecondPosition();
        long total = clip.getMicrosecondLength();
        if (total > 0) {
            progressBar.setValue((int) (1000.0 * current / total));
            elapsedLabel.setText(formatTime(current));
            totalLabel.setText(formatTime(total));
        }
        if (!clip.isRunning() && !paused && current >= total) {
            uiTimer.stop();
            statusLabel.setText("✓  Reproducción finalizada");
        }
    }

    private String formatTime(long micros) {
        long seconds = TimeUnit.MICROSECONDS.toSeconds(micros);
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private void closeClip() {
        if (uiTimer != null) uiTimer.stop();
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    @Override
    public void dispose() {
        closeClip();
        super.dispose();
    }

    // Gradient background
    private class GradientPanel extends JPanel {
        GradientPanel() {
            setLayout(new BorderLayout());
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gradient = new GradientPaint(0, 0, new Color(17, 20, 39),
                    getWidth(), getHeight(), new Color(35, 20, 65));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private class RoundedPanel extends JPanel {
        private final int radius;
        private final Color color;
        RoundedPanel(int radius, Color color) {
            this.radius = radius;
            this.color = color;
            setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            // Soft decorative glow
            g2.setColor(new Color(139, 92, 246, 35));
            g2.fillOval(getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            ReproductorMusical app = new ReproductorMusical();
            app.setVisible(true);
        });
    }
}
