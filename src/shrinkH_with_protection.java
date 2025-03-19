import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class shrinkH_with_protection {
    //水平方向上寻找最小能量路径，以达到增高的目的
    public static BufferedImage imag;
    public static void main (String[] args) {
        BufferedImage im=imag;
        execute(im);
    }
    public shrinkH_with_protection(BufferedImage i){
        imag=i;
    }
    public static void execute(BufferedImage image){
        try {
            if(image==null){
                image = ImageIO.read(new File("input.jpg"));
            }
            image.getRGB(0,0);
            Scanner in=new Scanner(System.in);
            System.out.println("Enter height(an integer) to be reduced:");
            int cut=in.nextInt();
            int newHeight = image.getHeight() - cut; // 要缩小成的高度

            BufferedImage resultImage = seamCarving(image, newHeight);

            File output = new File("output2.jpg");
            ImageIO.write(resultImage, "jpg", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static BufferedImage execute1 (BufferedImage image,int cut)throws Exception{
        if(image==null){
            image = ImageIO.read(new File("input.jpg"));
        }
        Scanner in=new Scanner(System.in);
        int newHeight = image.getHeight() - cut; // 要缩小成的高度
        BufferedImage resultImage = seamCarving(image, newHeight);
        File output = new File("output2.jpg");
        ImageIO.write(resultImage, "jpg", output);
        return resultImage;
    }

    private static BufferedImage seamCarving(BufferedImage image, int newHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        while (height > newHeight) {
            RectangleInput rectangleInput = new RectangleInput();
            int[][] energy = calculateEnergy(image,rectangleInput);
            int[] seam = findHorizontalSeam(energy);

            image = removeHorizontalSeam(image, seam);

            height--;
        }

        return image;
    }

    private static int[][] calculateEnergy(BufferedImage image,RectangleInput rectangleInput) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] energy = new int[width][height];

        Rectangle selectionRectangle = rectangleInput.getRectangle();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(y>=selectionRectangle.getY()&&y<=selectionRectangle.getY()+selectionRectangle.getHeight()){
                    if(x>=selectionRectangle.getX()&&x<=selectionRectangle.getX()+selectionRectangle.getWidth()){
                        energy[x][y] = Integer.MAX_VALUE; // Set energy to maximum
                    }
                }
                else energy[x][y] = calculatePixelEnergy(image, x, y);
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

    private static int[] findHorizontalSeam(int[][] energy) {
        int width = energy.length;
        int height = energy[0].length;
        int[][] dp = new int[width][height];
        int[][] path = new int[width][height];

        for (int x = 0; x < width; x++) {
            dp[x][0] = energy[x][0];
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dp[x][y] = energy[x][y] + dp[x][y-1];
                path[x][y] = x;

                if (x > 0 && dp[x-1][y-1] < dp[x][y]) {
                    dp[x][y] = energy[x][y] + dp[x-1][y-1];
                    path[x][y] = x - 1;
                }

                if (x < width - 1 && dp[x+1][y-1] < dp[x][y]) {
                    dp[x][y] = energy[x][y] + dp[x+1][y-1];
                    path[x][y] = x + 1;
                }
            }
        }

        int[] seam = new int[height];
        int minEnergy = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int x = 0; x < width; x++) {
            if (dp[x][height-1] < minEnergy) {
                minEnergy = dp[x][height-1];
                minIndex = x;
            }
        }

        for (int y = height - 1; y >= 0; y--) {
            seam[y] = minIndex;
            minIndex = path[minIndex][y];
        }

        return seam;
    }

    private static BufferedImage removeHorizontalSeam(BufferedImage image, int[] seam) {
        int width = image.getWidth() - 1;
        int height = image.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            int k = 0;
            for (int x = 0; x < width; x++) {
                if (x != seam[y]) {
                    newImage.setRGB(k, y, image.getRGB(x, y));
                    k++;
                }
            }
        }

        return newImage;
    }
}
