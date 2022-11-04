package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        this.circles = circles;

        PANEL_WIDTH = allowed;
        PANEL_HEIGHT = allowed*h/w;
        this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));
        this.setBackground(Color.black);
        timer = new Timer(1, this);
        timer.start();
    }

    public void paint(Graphics g) {

        super.paint(g); // paint background

        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.BLUE);
        drawCircles(g2D, circles);
        g2D.drawString(circleIndex+" circles drawn.", 100, 100);
    }

    private void drawCircles(Graphics2D g2D, java.util.List<Circle> circles) {
        g2D.setColor(Color.BLUE);
        IntStream.range(0, circleIndex).forEach(i -> drawDisc(g2D, circles.get(i)));
        g2D.setColor(Color.YELLOW);
        drawDisc(g2D, presentCircle);
        g2D.setColor(Color.BLUE);
    }

    private void drawDisc(Graphics2D g2D, Circle c) {
        int x = scaleX(c.x-c.r);
        int y = scaleX(c.y-c.r);

        g2D.fillOval(x, y, scaleX(2*c.r), scaleX(2*c.r));
    }

    int scaleX( int x){
        return PANEL_WIDTH*x/imgW;
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

    }
}
