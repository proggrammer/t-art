package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MyPanel extends JPanel implements ActionListener {
    int PANEL_WIDTH = 500;
    int PANEL_HEIGHT = 500;
    Timer timer;
    int allowed = 1200;
    java.util.List<Circle> circles;
    int imgW, imgH;
    int circleIndex;
    Circle presentCircle = null;
    MyPanel(java.util.List<Circle> circles, int w, int h){
        imgW = w;
        imgH = h;
        Comparator<Circle> comparator = Comparator.comparing(Circle::getR).thenComparing((c1,c2) -> -c1.getX()+c2.getX()).thenComparing((c1,c2) -> -c1.getY()+c2.getY()).reversed();
        this.circles = circles.stream().sorted(comparator).collect(Collectors.toList());

        PANEL_WIDTH = allowed;
        PANEL_HEIGHT = allowed*h/w;
        this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));
        this.setBackground(Color.BLACK);
        timer = new Timer(Math.max(0, (10-circleIndex/3)), this);
        timer.start();
    }

    public void paint(Graphics g) {

        super.paint(g); // paint background
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.black);
        drawCircles(g2D, circles);
        g2D.setColor(Color.blue);
        g2D.drawString("Picture of St. Xavier's, Kolkata, made of "+circles.size()+" circles only", 15, 25);
        g2D.drawString(+circleIndex+" circles drawn (T-art). Present radius:"+circles.get(circleIndex).r, 15, 40);
        g2D.drawString("Present radius:"+circles.get(circleIndex).r, 15, 65);
        if(circleIndex<circles.size())
            drawDisc(g2D, new Circle(350+circles.get(circleIndex).r,135+circles.get(circleIndex).r, circles.get(circleIndex).r));
    }
    int bounce=0;
    private void drawCircles(Graphics2D g2D, java.util.List<Circle> circles) {
        g2D.setColor(Color.white);
        IntStream.range(0, circleIndex).forEach(i -> drawDisc(g2D, circles.get(i)));
        bounce++;
        if(bounce == 16) bounce = 0;
        if(circleIndex < circles.size()) {
            g2D.setColor(Color.RED);
            drawDisc(g2D, new Circle(presentCircle.x, presentCircle.y, presentCircle.r + bounce + 8));
        }
        g2D.setColor(Color.YELLOW);
        drawDisc(g2D, presentCircle);

        g2D.setColor(Color.white);
    }

    private void drawDisc(Graphics2D g2D, Circle c) {
        int x = scaleX(c.x-c.r);
        int y = scaleX(c.y-c.r);

        g2D.fillOval(x, y, scaleX(2*c.r), scaleX(2*c.r));
    }

    private void drawCirc(Graphics2D g2D, Circle c) {
        int x = scaleX(c.x-c.r);
        int y = scaleX(c.y-c.r);

        g2D.drawOval(x, y, scaleX(2*c.r), scaleX(2*c.r));
    }

    int scaleX( int x){
        return (int) Math.round(PANEL_WIDTH*x*1.0/imgW);
    }

    int noteIndex=0;
    String notes = "CDEFGAB";

    @Override
    public void actionPerformed(ActionEvent e) {
        Circle targetCircle = circles.get(circleIndex);
        if(presentCircle == null)
            presentCircle = new Circle(targetCircle.x, targetCircle.y, 0);
        presentCircle = new Circle(presentCircle.x, presentCircle.y, presentCircle.r+1);
        repaint();
        if(presentCircle == null)
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ew) {
                throw new RuntimeException(ew);
            }
        if(presentCircle.r == targetCircle.r) {
            circleIndex++;
            presentCircle = new Circle(circles.get(circleIndex).x, circles.get(circleIndex).y, 0);
        }
        int delay = (int) Math.max(0, (10-Math.round(Math.log(circleIndex))));
        timer.setDelay(delay);
    }
}
