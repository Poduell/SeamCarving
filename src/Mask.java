import java.awt.*;

public class Mask {
    public static final int PROTECTION_MASK_VALUE = Integer.MAX_VALUE;;
    public static final int REMOVAL_MASK_VALUE = 0;
    private Rectangle rect;
    private int[][] mask;

    public Mask(int width, int height) {
        mask = new int[width][height];
        rect = new Rectangle(0, 0, 0, 0); // 初始化一个空矩形
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                mask[i][j]=1;
            }
        }
    }

    public void setRect(Rectangle rect) {
        this.rect = rect;
    }

    public Rectangle getRect() {
        return rect;
    }

    public void setMaskValue(int x, int y, int value) {
        if (isValidCoordinate(x, y)) {
            mask[x][y] = value;
        }
    }

    public int getMaskValue(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return mask[x][y];
        }
        return 0; // 默认返回 0
    }
    public boolean checkMaskValue() {
        int y=rect.y;
        int x= rect.x;;
        int height= rect.height;
        int width= rect.width;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                if(mask[x][y]==0)return false;
            }
        }
        return true;
    }
    public boolean updateMaskValue(int[] seam) {
        int y=rect.y;
        int x= rect.x;
        int height= rect.height;
        int width= rect.width;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                if(seam[i]==j)setMaskValue(i,j,1);
            }
        }
        return true;
    }

    public void setMaskByRect(Rectangle rect) {
        this.rect = rect;
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                setMaskValue(j, i, 0);
            }
        }
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < mask[0].length && y >= 0 && y < mask.length;
    }

    public void setStartPoint(Point point) {
        rect.setLocation(point);
    }

    public void setEndPoint(Point point) {
        int x = (int) Math.min(rect.getX(), point.getX());
        int y = (int) Math.min(rect.getY(), point.getY());
        int width = (int) Math.abs(rect.getX() - point.getX());
        int height = (int) Math.abs(rect.getY() - point.getY());
        rect.setBounds(x, y, width, height);
    }

}
