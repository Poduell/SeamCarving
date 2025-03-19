import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class bothextend {
    //同时缩小宽和高
    public static void main(String[] args) throws Exception{
        try {
            BufferedImage image = ImageIO.read(new File("input.jpg"));
            Scanner in=new Scanner(System.in);
            System.out.println("Enter width(an integer) to be reduced:");
            int cut=in.nextInt();
            System.out.println("Enter height(an integer) to be reduced:");
            int cut2=in.nextInt();
            extendHer rh=new extendHer(image);
            BufferedImage imm=rh.execute1(image,cut2);
            extendVer rv=new extendVer(imm);
            BufferedImage resultImage = rv.execute1(imm,cut);
            File output = new File("output5.jpg");
            ImageIO.write(resultImage, "jpg", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public BufferedImage execute1(BufferedImage i,int cut)throws  Exception{
        BufferedImage image = i;
        extendHer rh=new extendHer(image);
        BufferedImage imm=rh.execute1(image,cut);
        extendVer rv=new extendVer(imm);
        BufferedImage resultImage = rv.execute1(imm,cut);
        File output = new File("output5.jpg");
        ImageIO.write(resultImage, "jpg", output);
        return resultImage;
    }
}

