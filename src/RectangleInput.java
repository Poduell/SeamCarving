import java.awt.*;

public class RectangleInput {
    private Point startPoint;
    private Point endPoint;

    public RectangleInput() {
        startPoint = null;
        endPoint = null;
    }

    // 假设有一个方法可以获取鼠标当前位置
    public Point getCurrentMousePosition() {
        // 这里返回当前鼠标位置的假设实现
        return new Point(0, 0); // 这里返回 (0, 0)，实际应根据你的界面实现来获取鼠标位置
    }

    public Rectangle getRectangle() {
        if (startPoint == null || endPoint == null) {
            return null; // 如果起点或终点为空，返回空
        }

        // 使用起点和终点的坐标构造矩形对象
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        return new Rectangle(x, y, width, height);
    }

    public void updateStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public void updateEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    // 假设有一个方法用于检测鼠标按下事件
    public void mousePressed() {
        // 这里模拟鼠标按下事件，更新起点为当前鼠标位置
        startPoint = getCurrentMousePosition();
    }

    // 假设有一个方法用于检测鼠标释放事件
    public void mouseReleased() {
        // 这里模拟鼠标释放事件，更新终点为当前鼠标位置
        endPoint = getCurrentMousePosition();
    }
}

