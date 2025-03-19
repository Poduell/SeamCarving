import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class extendV_with_protection {
    public static BufferedImage imag;
    public static void main(String[] args) {
        BufferedImage im=imag;
        execute(im);
    }
    public extendV_with_protection(BufferedImage i){
        imag=i;
    }
    public static void execute(BufferedImage image){
        try {
            if(image==null){
                image = ImageIO.read(new File("input.jpg"));
            }
            imag=image;
            Scanner in=new Scanner(System.in);
            System.out.println("Enter height(an integer) to be extended:");
            int cut=in.nextInt();
            int newWidth = image.getWidth() + cut; // 要扩展成的宽度

            seamCarving(image, newWidth);

            File output = new File("output3.jpg");
            ImageIO.write(imag, "jpg", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static BufferedImage execute1(BufferedImage image,int cut) throws Exception{
        BufferedImage imag2=image;
        if (image == null) {
            image = ImageIO.read(new File("input.jpg"));
            imag2=ImageIO.read(new File("input.jpg"));
        }
        Scanner in = new Scanner(System.in);
        int newWidth = image.getWidth() + cut;
        seamCarving(image, newWidth);
        File output = new File("output3.jpg");
        ImageIO.write(imag, "jpg", output);
        return imag;
    }

    private static void seamCarving(BufferedImage image,int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();

        while (width < newWidth) {
            RectangleInput rectangleInput = new RectangleInput();
            int[][] energy = calculateEnergy(image,rectangleInput);

            int[] seam = findVerticalSeam(energy);

            image = removeVerticalSeam(image, seam);
            imag = extendVerticalSeam(imag,seam);

            width++;
        }
    }

    private static int[][] calculateEnergy(BufferedImage image,RectangleInput rectangleInput){
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] energy = new int[height][width];
        Rectangle selectionRectangle = rectangleInput.getRectangle();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(y>=selectionRectangle.getY()&&y<=selectionRectangle.getY()+selectionRectangle.getHeight()){
                    if(x>=selectionRectangle.getX()&&x<=selectionRectangle.getX()+selectionRectangle.getWidth()){
                        energy[y][x] = Integer.MAX_VALUE; // Set energy to maximum
                    }
                }
                else energy[y][x] = calculatePixelEnergy(image, x, y);
            }
        }


        return energy;
    }

    private static int calculatePixelEnergy(BufferedImage image, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();

        int leftX = (x == 0) ? width - 1 : x - 1;
        int rightX = (x == width - 1) ? 0 : x + 1;
        int upY = (y == 0) ? height - 1 : y - 1;
        int downY = (y == height - 1) ? 0 : y + 1;

        int rgbLeft = image.getRGB(leftX, y);
        int rgbRight = image.getRGB(rightX, y);
        int rgbUp = image.getRGB(x, upY);
        int rgbDown = image.getRGB(x, downY);

        int redLeft = (rgbLeft >> 16) & 0xFF;
        int greenLeft = (rgbLeft >> 8) & 0xFF;
        int blueLeft = rgbLeft & 0xFF;

        int redRight = (rgbRight >> 16) & 0xFF;
        int greenRight = (rgbRight >> 8) & 0xFF;
        int blueRight = rgbRight & 0xFF;

        int redUp = (rgbUp >> 16) & 0xFF;
        int greenUp = (rgbUp >> 8) & 0xFF;
        int blueUp = rgbUp & 0xFF;

        int redDown = (rgbDown >> 16) & 0xFF;
        int greenDown = (rgbDown >> 8) & 0xFF;
        int blueDown = rgbDown & 0xFF;

        int gradientX = (redLeft - redRight) * (redLeft - redRight) +
                (greenLeft - greenRight) * (greenLeft - greenRight) +
                (blueLeft - blueRight) * (blueLeft - blueRight);

        int gradientY = (redUp - redDown) * (redUp - redDown) +
                (greenUp - greenDown) * (greenUp - greenDown) +
                (blueUp - blueDown) * (blueUp - blueDown);

        return gradientX + gradientY;
    }

    private static int[] findVerticalSeam(int[][] energy) {
        int width = energy[0].length;
        int height = energy.length;
        int[][] dp = new int[width][height];
        int[][] path = new int[width][height];

        for (int y = 0; y < height; y++) {
            dp[0][y] = energy[y][0];
        }

        for (int x = 1; x < width; x++) {
            for (int y = 0; y < height; y++) {
                dp[x][y] = energy[y][x] + dp[x-1][y];
                path[x][y] = y;

                if (y > 0 && dp[x-1][y-1] < dp[x][y]) {
                    dp[x][y] = energy[y][x] + dp[x-1][y-1];
                    path[x][y] = y - 1;
                }

                if (y < height - 1 && dp[x-1][y+1] < dp[x][y]) {
                    dp[x][y] = energy[y][x] + dp[x-1][y+1];
                    path[x][y] = y + 1;
                }
            }
        }

        int[] seam = new int[width];
        int minEnergy = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int y = 0; y < height; y++) {
            if (dp[width-1][y] < minEnergy) {
                minEnergy = dp[width-1][y];
                minIndex = y;
            }
        }

        for (int x = width - 1; x >= 0; x--) {
            seam[x] = minIndex;
            minIndex = path[x][minIndex];
        }

        return seam;
    }

    private static BufferedImage removeVerticalSeam(BufferedImage image, int[] seam) {
        int width = image.getWidth();
        int height = image.getHeight() - 1; // 要缩小成的高度

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            int k = 0;
            for (int y = 0; y < height; y++) {
                if (y != seam[x]) {
                    newImage.setRGB(x, k, image.getRGB(x, y));
                    k++;
                }
            }
        }

        return newImage;
    }
    private static BufferedImage extendVerticalSeam(BufferedImage image, int[] seam) {
        int width = image.getWidth();
        int height = image.getHeight() + 1; // 要扩展成的高度

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            int k = 0;
            for (int y = 0; y < height; y++) {
                if (y <= seam[x]) {
                    newImage.setRGB(x, k, image.getRGB(x, y));
                    k++;
                }
                else{
                    newImage.setRGB(x, k, image.getRGB(x, y-1));
                    k++;
                }
            }
        }

        return newImage;
    }
}

