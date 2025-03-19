import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;

public class ImageProcessingAppBackUp extends JFrame {
    private JLabel statusLabel;
    private JLabel imageLabel;
    private JPanel canvas;
    private BufferedImage originalImage;
    private ArrayList<Point> penPositions;

    public ImageProcessingAppBackUp() {
        setTitle("Image Processing App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem importItem = new JMenuItem("Import Image");
        importItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                importImage();
            }
        });
        fileMenu.add(importItem);
        JMenuItem exportItem = new JMenuItem("Export Pen Positions");
        exportItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportPenPositions();
            }
        });
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        statusLabel = new JLabel("Status: ");
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        canvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                if (originalImage != null) {
                    g2d.drawImage(originalImage, 0, 0, null);
                }

                if (penPositions != null && penPositions.size() > 1) {
                    g2d.setColor(Color.RED);
                    for (int i = 0; i < penPositions.size() - 1; i++) {
                        Point p1 = penPositions.get(i);
                        Point p2 = penPositions.get(i + 1);
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                if (originalImage != null) {
                    return new Dimension(originalImage.getWidth(), originalImage.getHeight());
                }
                return super.getPreferredSize();
            }
        };
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (originalImage != null) {
                    if (penPositions == null) {
                        penPositions = new ArrayList<>();
                    }
                    penPositions.add(e.getPoint());
                    canvas.repaint();
                }
            }
        });
        mainPanel.add(canvas, BorderLayout.CENTER);

        imageLabel = new JLabel();
        mainPanel.add(imageLabel, BorderLayout.EAST);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void importImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(fileChooser.getSelectedFile());
                imageLabel.setText("Image Size: " + originalImage.getWidth() + " x " + originalImage.getHeight());
                statusLabel.setText("Status: Image imported successfully");
                canvas.repaint();
            } catch (IOException ex) {
                statusLabel.setText("Status: Error importing image");
                ex.printStackTrace();
            }
        }
    }
    public void importImage(BufferedImage image) {
        originalImage = image;
        imageLabel.setText("Image Size: " + originalImage.getWidth() + " x " + originalImage.getHeight());
        statusLabel.setText("Status: Image imported successfully");
        canvas.repaint();
    }

    public void exportPenPositions() {
        if (penPositions != null && penPositions.size() > 0) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    for (Point point : penPositions) {
                        writer.println(point.x + "," + point.y);
                    }
                    statusLabel.setText("Status: Pen positions exported successfully");
                } catch (IOException ex) {
                    statusLabel.setText("Status: Error exporting pen positions");
                    ex.printStackTrace();
                }
            }
        } else {
            statusLabel.setText("Status: No pen positions to export");
        }
    }
    public BufferedImage getSelectedImage() {
        return originalImage;
    }
    public ArrayList<Point> getPenPositions() {
        return penPositions;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ImageProcessingAppBackUp();
            }
        });
    }
}
