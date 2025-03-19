import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessingApp extends JFrame {
    private JLabel statusLabel;
    private JLabel imageLabel;
    private JPanel canvas;
    private BufferedImage originalImage;
    private Mask mask;
    private JLabel sidebarLabel; // 新增的边栏标签
    private BufferedImage proImage = ImageIO.read(new File("input.jpg"));
    private Integer num = 0;
    private Integer choice = 0;
    public ImageProcessingApp() throws IOException {
        setTitle("Image Processing GUI");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem importItem = new JMenuItem("Import Image");
        importItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //BufferedImage currentImage = null;
                importImage();
            }
        });
        fileMenu.add(importItem);
        JMenuItem exportItem = new JMenuItem("Export Processed Image");
        exportItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportImage();
                updateCarverGUIImage();
            }
        });
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        statusLabel = new JLabel("Status: ");
        statusLabel.setBounds(0,0,200,10);
        mainPanel.add(statusLabel);

        // 创建边栏标签
        sidebarLabel = new JLabel("Start Point: (0, 0)   End Point: (0, 0)");
        statusLabel.setBounds(0,10,300,10);
        mainPanel.add(sidebarLabel);

        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                if (originalImage != null) {
                    g2d.drawImage(originalImage, 0, 0, null);
                }

                if (mask != null) {
                    Rectangle rect = mask.getRect();
                    if (rect != null) {
                        g2d.setColor(Color.RED);
                        g2d.draw(rect);
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
                    if (mask == null) {
                        mask = new Mask(originalImage.getWidth(), originalImage.getHeight());
                    }
                    mask.setStartPoint(e.getPoint());
                    canvas.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (mask != null) {
                    mask.setEndPoint(e.getPoint());
                    canvas.repaint();
                    try {
                        showProtectionOrRemovalDialog();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainPanel, "Please select a smaller region!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        imageLabel = new JLabel();
        imageLabel.setBounds(0,30,300,10);
        mainPanel.add(imageLabel);
        canvas.setBounds(0,0,mainPanel.getWidth(), mainPanel.getHeight());
        mainPanel.add(canvas);
        setContentPane(mainPanel);
        setVisible(true);
    }
    private void importImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(fileChooser.getSelectedFile());
                proImage = originalImage;
                imageLabel.setText("Image Size: " + originalImage.getWidth() + " x " + originalImage.getHeight());
                statusLabel.setText("Status: Image imported successfully");
                canvas.repaint();
            } catch (IOException ex) {
                statusLabel.setText("Status: Error importing image");
                ex.printStackTrace();
            }
        }
    }
    public void importImage(BufferedImage currentImage) {
        originalImage = currentImage;
        proImage = originalImage;
        imageLabel.setText("Image Size: " + originalImage.getWidth() + " x " + originalImage.getHeight());
        statusLabel.setText("Status: Image imported successfully");
        canvas.setPreferredSize(new Dimension(originalImage.getWidth(), originalImage.getHeight()));
        this.pack();
        canvas.repaint();
    }
    public void importChoice(int num) {
        choice = num;
    }
    public void importAmount(int number) {
        num = number;
    }

    private void exportRectangleToMask() {
        if (mask != null) {
            Rectangle rect = mask.getRect();
            if (rect != null) {
                mask.setRect(rect);
                statusLabel.setText("Status: Rectangle exported to Selected.Mask successfully");
            } else {
                statusLabel.setText("Status: No rectangle to export");
            }
        } else {
            statusLabel.setText("Status: No rectangle to export");
        }
    }
    private void updateSidebarLabel() {
        // 更新边栏标签显示
        if (mask != null) {
            Rectangle rect = mask.getRect();
            if (rect != null) {
                int x1 = (int) rect.getX();
                int y1 = (int) rect.getY();
                int x2 = (int) rect.getMaxX();
                int y2 = (int) rect.getMaxY();
                sidebarLabel.setText("Start Point: (" + x1 + ", " + y1 + ")   End Point: (" + x2 + ", " + y2 + ")");
            }
        }
    }
    private void showProtectionOrRemovalDialog() throws Exception {
        updateSidebarLabel();
        if (choice == 0) {
            // 用户选择保护区域
            protectSelectedRegion(); // 在这里调用 protectSelectedRegion() 方法
        } else if (choice == 1) {
            // 用户选择删除区域
            removeSelectedRegion(); // 在这里调用 removeSelectedRegion() 方法
        }
    }



    //保护操作的缩放
    private void protectSelectedRegion() throws Exception {
        // 根据 mask 对图像进行保护操作
        Rectangle selectedRect = mask.getRect();

        // 检查是否选择了有效的区域
        if (selectedRect != null) {
            // 弹出对话框，让用户选择是扩展还是缩小保护区域
            String[] options = {"Expand", "Shrink"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Do you want to expand or shrink the protected region?",
                    "Expand or Shrink Protection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                // 用户选择扩展保护区域
                expandProtectedRegion();
            } else if (choice == JOptionPane.NO_OPTION) {
                // 用户选择缩小保护区域
                shrinkProtectedRegion();
            }
            canvas.setPreferredSize(new Dimension(originalImage.getWidth(), originalImage.getHeight()));

            // 自动调整窗口大小以适应组件
            this.pack();
        } else {
            JOptionPane.showMessageDialog(this, "No region selected.");
        }
    }

    // 扩展保护区域
    private void expandProtectedRegion() throws Exception {
        BufferedImage copy1 = proImage;
        BufferedImage copy2 = copy1;
        extendV_with_protection extendVer1 = new extendV_with_protection(copy1);
        extendH_with_protection extendHer1 = new extendH_with_protection(copy2);
        BufferedImage temp =  extendH_with_protection.execute1(proImage,num);
        proImage = extendV_with_protection.execute1(temp,num);
        refreshImageDisplay();
    }

    // 缩小保护区域
    private void shrinkProtectedRegion() throws Exception {
        BufferedImage copy1 = proImage;
        BufferedImage copy2 = copy1;
        shrinkH_with_protection shrinkHer1 = new shrinkH_with_protection(copy1);
        shrinkV_with_protection shrinkVer1 = new shrinkV_with_protection(copy2);
        BufferedImage temp = shrinkH_with_protection.execute1(proImage,num);
        proImage = shrinkV_with_protection.execute1(temp,num);
        refreshImageDisplay();
    }


    public void exportImage(){
        File processedImage = new File("processedImage.jpg");
        try {
            ImageIO.write(proImage, "jpg", processedImage);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
//消除并缩放
    private void removeSelectedRegion() throws Exception {
        // 根据 mask 对图像进行保护操作
        Rectangle selectedRect = mask.getRect();
        // 检查是否选择了有效的区域
        if (selectedRect != null) {
            // 弹出对话框，让用户选择是扩展还是缩小保护区域
            String[] options = {"Yes", "No"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Sure to edit this region?",
                    "Expand or Shrink Protection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                // 用户选择扩展保护区域
                EditSelectedRegion(selectedRect);
            } else if (choice == JOptionPane.NO_OPTION) {
                // 用户选择缩小保护区域

            }
            canvas.setPreferredSize(new Dimension(originalImage.getWidth(), originalImage.getHeight()));

            // 自动调整窗口大小以适应组件
            this.pack();
        } else {
            JOptionPane.showMessageDialog(this, "No region selected.");
        }
    }
    private void EditSelectedRegion(Rectangle selectedRect) throws Exception {
        BufferedImage copy1 = proImage;
        deleteHer extendHer1 = new deleteHer(copy1,mask.getRect());
        proImage = extendHer1.execute1(copy1,mask.getRect());
        refreshImageDisplay();
    }
    public BufferedImage updateImage(){
        return proImage;
    }
    public void updateCarverGUIImage() {
        // 确保 processedImage.jpg 文件已经存在并且包含导出的图像
        try {
            File processedImageFile = new File("processedImage.jpg");
            if (processedImageFile.exists()) {
                BufferedImage updatedImage = ImageIO.read(processedImageFile);
                // 假设 CarverGUI 实例存储在一个可访问的变量中
                CarverGUI.currentImage = updatedImage;
                // 假设 drop 组件需要更新显示
                CarverGUI.drop.setIcon(new ImageIcon(updatedImage));
                CarverGUI.drop.revalidate();
                CarverGUI.drop.repaint();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void refreshImageDisplay() {
        originalImage = proImage;

        canvas.repaint();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new ImageProcessingApp();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}