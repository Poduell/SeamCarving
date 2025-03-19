import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class deleteHer {
    public static BufferedImage imag;
    public static Rectangle rect;
    public static int lang=0;
    public static void main(String[] args) {
        BufferedImage im=imag;
    }
    public deleteHer(BufferedImage i,Rectangle r){
        imag=i;
        rect=r;
    }
    public static BufferedImage execute1(BufferedImage image,Rectangle r) throws Exception{
        if(image==null){
            image = ImageIO.read(new File("input.jpg"));
        }
        rect=r;
        imag=image;
        lang=(int)rect.getHeight();
        seamCarving(image);
        extendHer ex=new extendHer(imag);
        imag=ex.execute1(imag,lang);
        return imag;
    }

    private static void seamCarving(BufferedImage image) {
        int nb=0;
        while (nb<lang) {
            int[][] energy = calculateEnergy(imag,nb);
            int[] seam = findVerticalSeam(energy);
            imag = removeVerticalSeam(imag, seam);
            nb++;
            rect.height--;
        }
        //nb=0;
        //while(nb<lang){
            //int[][] energy = calculateEnergy(image,nb);
            //int[] seam = findVerticalSeam(energy);
            //imag = extendVerticalSeam(imag,seam);
            nb++;
        //}
    }
    private static int[][] calculateEnergy(BufferedImage image,int nb) {
        //有框选区域的，从最后一行开始赋最小值，每次把最后一行删了，尽量不要影响到前面
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] energy = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(x>=rect.x&&x<=rect.x+rect.width){
                    if(y>=rect.y&&y<rect.y+ rect.height){
                        energy[y][x]=Integer.MAX_VALUE;
                    }
                    else if(y==rect.y+ rect.height){
                        energy[y][x]=Integer.MIN_VALUE;
                    }
                    else energy[y][x] = calculatePixelEnergy(image, x, y);
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

    public static BufferedImage removeVerticalSeam(BufferedImage image, int[] seam) {
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
    public static BufferedImage extendVerticalSeam(BufferedImage image, int[] seam) {
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
