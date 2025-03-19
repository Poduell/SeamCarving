import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CarverGUI extends JFrame {
    public static void main(String[] args) {
        new CarverGUI();
    }
    public CarverGUI() {
        this.setTitle("Seam-Carver");
        String path = "src/img/Snapshot0.png";
        ImageIcon background = new ImageIcon(path);
        JLabel bg = new JLabel(background);
        this.setSize( 600, 500);
        this.setLocationRelativeTo(null);
        bg.setBounds(0, 0,600, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getLayeredPane().add(bg,Integer.MIN_VALUE);
        System.out.println("Group 17 Seamcarving project");
        JButton button = new JButton("Start Carving");
        button.setBackground(new Color(0,0,0,0));
        button.setBounds(100,100,400,120);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE,10));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD,  50));
        button.addActionListener(e -> {
            try {
                showOperatePanel();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        this.getLayeredPane().add(button);
        this.setVisible(true);
    }
    public static BufferedImage currentImage;
    public static JLabel drop;
    public JTextField shrinkField = new JTextField(5);
    public JTextField pixField = new JTextField(5);
    private ImageProcessingApp imageProcessingApp;
    public String currentMode = "Both";
    private void showOperatePanel() throws IOException {
        JFrame newWindow = new JFrame("Seam-Carver - Operate Panel");
        List<BufferedImage> videoFrames = new ArrayList<>();

        shrinkField.setBounds(280, 620, 80, 40);
        shrinkField.setFont(new Font("Arial", Font.BOLD,  15));
        shrinkField.setBorder(BorderFactory.createLineBorder(new Color(19, 71, 120, 255),3));
        pixField.setBounds(480, 620, 80, 40);
        pixField.setBorder(BorderFactory.createLineBorder(new Color(19, 71, 120, 255),3));
        shrinkField.setToolTipText("Carve amount");
        pixField.setToolTipText("Image size");
        newWindow.setSize(1000, 730);
        newWindow.setLocationRelativeTo(null);
        newWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newWindow.setLayout(null);
        String path1 = "src/img/fade.jpg";
        ImageIcon bgIcon = new ImageIcon(path1);
        JLabel bg1 = new JLabel(bgIcon);
        bg1.setBounds(0, 0, 1000, 730);
        newWindow.add(bg1);
        dropPanel(newWindow);
        addButtons(newWindow);
        newWindow.getLayeredPane().add(shrinkField, JLayeredPane.DRAG_LAYER);
        newWindow.getLayeredPane().add(pixField, JLayeredPane.DRAG_LAYER);
        newWindow.setVisible(true);
        this.dispose();
    }
    private void dropPanel(JFrame frame) throws IOException {
        String path2 = "src/img/cloud-upload0.png";
        ImageIcon dropIcon  = new ImageIcon(path2);
        JLabel drop0 = new JLabel(dropIcon);
        drop0.setBounds(50, 50,750, 550);
        drop0.setBorder(BorderFactory.createLineBorder(Color.WHITE,10,true));
        drop = new JLabel(dropIcon);
        drop.setBounds(50, 50,750, 550);
        drop.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable transferable = evt.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        File droppedFile = droppedFiles.get(0);
                        ImageIcon icon = new ImageIcon(droppedFile.getAbsolutePath());
                        Image img = icon.getImage();
                        int imgWidth = img.getWidth(null);
                        int imgHeight = img.getHeight(null);
                        double scaleFactor = (double) 530 / imgHeight;
                        int newWidth = (int) (imgWidth * scaleFactor);
                        int newX = 375 - newWidth / 2;
                        int newY = 325 - imgHeight / 2;
                        ImageIcon scaledIcon = new ImageIcon(img.getScaledInstance(newWidth, 530, Image.SCALE_SMOOTH));
                        currentImage = new BufferedImage(scaledIcon.getIconWidth(), scaledIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = currentImage.createGraphics();
                        g2d.drawImage(scaledIcon.getImage(), 0, 0, null);
                        g2d.dispose();
                        try {
                            currentImage = ImageIO.read(new FileInputStream(droppedFile.getAbsolutePath()));
                            currentImage = resize(currentImage,530);
                            updatePixField();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        File input = new File("input.jpg");
                        try {
                            ImageIO.write(currentImage, "jpg", input);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println(currentImage.getHeight()+"x"+currentImage.getWidth());
                        drop.setIcon(scaledIcon);
                        drop.setBounds(newX, newY, scaledIcon.getIconWidth(), scaledIcon.getIconHeight());
                        drop.revalidate();
                        drop.repaint();
                    }
                } catch (IOException | UnsupportedFlavorException | IllegalArgumentException e) {
                    e.printStackTrace();
                    evt.dropComplete(false);
                    return;
                }
                evt.dropComplete(true);
            }
        });
        drop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFormatNames()));
                    int result = fileChooser.showOpenDialog(CarverGUI.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                        Image img = icon.getImage();
                        int imgWidth = img.getWidth(null);
                        int imgHeight = img.getHeight(null);
                        double scaleFactor = (double) 530 / imgHeight;
                        int newWidth = (int) (imgWidth * scaleFactor);
                        ImageIcon scaledIcon = new ImageIcon(img.getScaledInstance(newWidth, 530, Image.SCALE_SMOOTH));
                        currentImage = new BufferedImage(scaledIcon.getIconWidth(), scaledIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = currentImage.createGraphics();
                        g2d.drawImage(scaledIcon.getImage(), 0, 0, null);
                        g2d.dispose();
                        try {
                            currentImage = ImageIO.read(new FileInputStream(selectedFile.getAbsolutePath()));//TODO
                            currentImage = resize(currentImage,530);
                            updatePixField();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        //currentImage
                        File input = new File("input.jpg");
                        try {
                            ImageIO.write(currentImage, "jpg", input);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println(currentImage.getHeight()+"x"+currentImage.getWidth());
                        drop.setIcon(scaledIcon);
                        drop.revalidate();
                        drop.repaint();
                    }
                }
            }
        });
        frame.getLayeredPane().add(drop);
        frame.getLayeredPane().add(drop0, Integer.MIN_VALUE);
    }
    private void controlButton(int x, int y, int letterSize, String path3, String text, JFrame frame, Runnable action){
        ImageIcon buttonIcon  = new ImageIcon(path3);
        JLabel buttonLabel = new JLabel(buttonIcon);
        Image img = buttonIcon.getImage();
        ImageIcon scaledIcon = new ImageIcon(img.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        buttonLabel.setIcon(scaledIcon);
        buttonLabel.setBounds(x, y, scaledIcon.getIconWidth(), scaledIcon.getIconHeight());
        JButton buttonWhat = new JButton(text);
        buttonWhat.setBackground(new Color(255, 255, 255, 255));
        buttonWhat.setBounds(x + 45,y,80,40);
        buttonWhat.setBorder(BorderFactory.createLineBorder(new Color(19, 71, 120, 255),5));
        buttonWhat.setForeground(new Color(19, 71, 120, 255));
        buttonWhat.setFont(new Font("Arial", Font.BOLD,  letterSize));
        buttonWhat.addActionListener(e -> action.run());
        frame.getLayeredPane().add(buttonLabel);
        frame.getLayeredPane().add(buttonWhat);
    }
    private void addButtons(JFrame frame){
        controlButton(820,100,12,"src/img/backward-time.png","RESTORE",frame,() -> {
            pixField.setText("0 x 0");
            drop.setIcon(null);
            drop.revalidate();
            drop.repaint();
        });
        controlButton(820,200,15,"src/img/confirmed.png","REMOVE",frame,() -> {
            if (currentImage != null) {
                try {
                    imageProcessingApp = new ImageProcessingApp();
                String inputText = shrinkField.getText();
                int amount = Integer.parseInt(inputText);
                imageProcessingApp.importImage(currentImage);
                imageProcessingApp.importChoice(1);
                imageProcessingApp.importAmount(amount);
                imageProcessingApp.setSize(currentImage.getWidth(), currentImage.getHeight());
                imageProcessingApp.setLocationRelativeTo(CarverGUI.this);
                imageProcessingApp.setVisible(true);
                imageProcessingApp.updateCarverGUIImage();
                ImageIcon newIcon = new ImageIcon(currentImage);
                //System.out.println(currentImage.getHeight()+"x"+currentImage.getWidth());
                updatePixField();
                drop.setIcon(newIcon);
                drop.revalidate();
                drop.repaint();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "Please select a smaller region!", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        controlButton(820,300,12,"src/img/cancel.png","PROTECT",frame,() -> {
            if (currentImage != null) {
                try {
                    imageProcessingApp = new ImageProcessingApp();
                    String inputText = shrinkField.getText();
                    int amount = Integer.parseInt(inputText);
                    imageProcessingApp.importImage(currentImage);
                    imageProcessingApp.importChoice(0);
                    imageProcessingApp.importAmount(amount);
                    imageProcessingApp.setSize(currentImage.getWidth(), currentImage.getHeight());
                    imageProcessingApp.setLocationRelativeTo(CarverGUI.this);
                    imageProcessingApp.setVisible(true);
                    imageProcessingApp.updateCarverGUIImage();
                    ImageIcon newIcon = new ImageIcon(currentImage);
                    //System.out.println(currentImage.getHeight()+"x"+currentImage.getWidth());
                    updatePixField();
                    drop.setIcon(newIcon);
                    drop.revalidate();
                    drop.repaint();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "Please select a smaller region!", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        controlButton(820,405,15,"src/img/save.png","SAVE",frame,() ->{
            BufferedImage imageToSave = currentImage;
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFormatNames()));
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String fileName = selectedFile.getName();
                    if (!fileName.endsWith(".png")) {
                        fileName += ".png";
                    }
                    selectedFile = new File(selectedFile.getParent(), fileName);
                    ImageIO.write(imageToSave, "png", selectedFile);
                    JOptionPane.showMessageDialog(frame, "Successfully saved!");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "Error while saving!\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        controlButton(820,500, 15, "src/img/usable.png", "MODE", frame, () -> {
            String[] options = {"Horizontal", "Vertical", "Both"};
            int choice = JOptionPane.showOptionDialog(frame,
                    "Choose an option:", "Mode Selection",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);

            if (choice != -1) {
                currentMode = options[choice];
            }
        });
        controlButton(70, 620, 15, "src/img/contract.png", "SHRINK", frame, () -> SwingUtilities.invokeLater(() -> {
            try {
                String inputText = shrinkField.getText();
                int amount = Integer.parseInt(inputText);
                if ("Horizontal".equals(currentMode)) {
                    currentImage = removeVer.execute1(currentImage, amount);
                } else if ("Vertical".equals(currentMode)) {
                    currentImage = removeHer.execute1(currentImage, amount);
                } else if ("Both".equals(currentMode)) {
                    BufferedImage middleImage;
                    middleImage = removeHer.execute1(currentImage, amount);
                    currentImage = removeVer.execute1(middleImage, amount);
                }
                ImageIcon newIcon = new ImageIcon(currentImage);
                System.out.println(currentImage.getHeight()+"x"+currentImage.getWidth());
                updatePixField();
                drop.setIcon(newIcon);
                drop.revalidate();
                drop.repaint();
            } catch (Exception nfe) {
                JOptionPane.showMessageDialog(frame, "Number not valid!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }));
        controlButton(600,620, 15, "src/img/expand.png", "EXPAND", frame, () -> SwingUtilities.invokeLater(() -> {
            try {
                BufferedImage copy1 = currentImage;
                BufferedImage copy2 = copy1;
                extendHer extendHer1 = new extendHer(copy1);
                extendVer extendVer1 = new extendVer(copy2);
                String input = shrinkField.getText();
                int amount = Integer.parseInt(input);
                if ("Horizontal".equals(currentMode)) {
                    currentImage = extendHer1.execute1(currentImage, amount);
                }
                else if ("Vertical".equals(currentMode)) {
                    currentImage = extendVer1.execute1(currentImage, amount);
                }
                else if ("Both".equals(currentMode)) {
                    bothextend de=new bothextend();
                    currentImage = de.execute1(currentImage, amount);
                }
                ImageIcon newIcon = new ImageIcon(currentImage);

                updatePixField();
                drop.setIcon(newIcon);
                drop.revalidate();
                drop.repaint();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Number not valid!", "Input Error", JOptionPane.ERROR_MESSAGE);

            }
        }));
        controlButton(850,650,15,null,"EXIT",frame,(frame::dispose));
    }
    private void updatePixField() {
        if (currentImage != null) {
            String dimensions = currentImage.getWidth() + "x" + currentImage.getHeight();
            SwingUtilities.invokeLater(() -> {
                pixField.setText(dimensions);
            });
        }
    }
    public static BufferedImage resize(BufferedImage img, int newH) {
        int newW = img.getWidth()*530/ img.getHeight();
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }
    /*
    ImageIcon newIcon1 = new ImageIcon(currentImage);
    JFrame testFrame = new JFrame("test");
    JLabel testLabel = new JLabel("test");
    testLabel.setBounds(0,0, newIcon1.getIconWidth(), newIcon1.getIconHeight());
    testFrame.setBounds(100,100,1000,1000);
    testLabel.setIcon(newIcon1);
    testFrame.getLayeredPane().add(testLabel);
    testFrame.setVisible(true);*/
}

