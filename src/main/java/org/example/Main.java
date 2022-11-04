package org.example;

import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.jfugue.theory.ChordProgression;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static int MAX_NUM_OF_CIRCLES = 500;

    public static void main(String[] args) {
        String jpgPath = "/Users/kapil.rajak/development/t-art/t-art/src/main/resources/shampoo1.jpg";//input
        String svgPath = "/Users/kapil.rajak/development/t-art/t-art/src/main/resources/shampoo1.jpg";//svg output
        /* write svg */
        Point2D wh = new Point2D(0,0);
        List<Circle> reducedResult = circleCompute(wh, jpgPath);
        writeSVG(svgPath, reducedResult, wh.x, wh.y);

        /* music */
        new Thread(() -> {
            while (true) {
                Pattern pattern = new ChordProgression("I IV V")
                        .distribute("7%6")
                        .allChordsAs("$0 $0 $0 $0 $1 $1 $0 $0 $2 $1 $0 $0")
                        .eachChordAs("$0ia100 $1ia80 $2ia80 $3ia80 $4ia100 $3ia80 $2ia80 $1ia80")
                        .getPattern()
                        .setInstrument("Acoustic_Bass")
                        .setTempo(100);
                new Player().play(pattern);
            }
        }).start();
        /* read circles */
        List<Circle> circles = readCirclesFromSVG(svgPath, wh);

        /* drawAnimations circles */
        new MyFrame(circles, wh.x, wh.y);
    }

    private static List<Circle> readCirclesFromSVG(String path, Point2D wh) {
        try {
            List<String> lines =
                    Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            List<Circle> circles = lines.stream().filter( s-> s.contains("circle")).map(s -> getCircle(s)).collect(Collectors.toList());

            String whS = lines.stream().filter( s-> s.contains("width")).collect(Collectors.toList()).get(0);
            wh.x = Integer.valueOf(whS.split("\"")[1]);
            wh.y = Integer.valueOf(whS.split("\"")[3]);
            return circles;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Circle getCircle(String s) {
        int cx = Integer.valueOf(s.split("\"")[1]);
        int cy = Integer.valueOf(s.split("\"")[3]);
        int r = Integer.valueOf(s.split("\"")[5]);
        return new Circle(cx,cy,r);
    }

    private static List<Circle> circleCompute(Point2D point2D, String jpgPath)	{
        try {
            BufferedImage bufferedImage= ImageIO.read(new File(jpgPath));

            int width = bufferedImage.getWidth(null);
            int height = bufferedImage.getHeight(null);
            boolean[][] pixels = new boolean[width][height];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixels[i][j] = isWhite(bufferedImage.getRGB(i, j));
                }
            }
            point2D = new Point2D(width, height);
            System.out.println("b:"+black+", w:"+white);
            long sTime = System.currentTimeMillis();
            List<Circle> circlesAtEachWhitePoint = getMaxCircleAtEachWhitePoint(pixels);
            System.out.println("\nTime taken: "+(System.currentTimeMillis() - sTime)+"#Circles individually computed:"+circlesAtEachWhitePoint.size());
            return reducedResultFinal(circlesAtEachWhitePoint, pixels);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeSVG(String svgPath, List<Circle> reducedResult, int w, int h) {
        String content = "<svg version=\"1.1\"\n" +
                "style=\"background-color:black\"\n"+
                "     width=\""+w+"\" height=\""+h+"\"\n" +
                "     xmlns=\"http://www.w3.org/2000/svg\">\n" +
                reducedResult.stream().map(c -> "<circle cx=\""+c.x+"\" cy=\""+c.y+"\" r=\""+c.r+"\" fill=\"white\" />\n").collect(Collectors.joining("")) +
                "</svg>";
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(svgPath);
            myWriter.write(content);
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Successfully wrote to the file.");
    }

    private static List<Circle> reducedResultFinal(List<Circle> circleList, boolean[][] pixels) {
        List<Circle> finalResult = new ArrayList<>();
        System.out.println("Reduction Started");
        while(!circleList.isEmpty() && finalResult.size()<MAX_NUM_OF_CIRCLES) {
            Circle maxC = circleList.stream().max(Comparator.comparingInt(c -> c.r)).get();
            finalResult.add(maxC);
            List<Circle> notConnectedCircles = circleList.stream().filter(c -> !(maxC.x == c.x && maxC.y == c.y) && !interSectingCircle(c, maxC)).collect(Collectors.toList());
            List<Circle> connectedCircles = circleList.stream().filter(c -> !(maxC.x == c.x && maxC.y == c.y) && interSectingCircle(c, maxC)).collect(Collectors.toList());
            List<Circle> recomputedCircles = connectedCircles.stream().map(c -> maxCircleCalc(c.x, c.y, pixels, finalResult)).collect(Collectors.toList());
            circleList = Stream.concat(notConnectedCircles.stream(), recomputedCircles.stream()).collect(Collectors.toList());
            if(finalResult.size()%50==0)
                System.out.print(finalResult.size()+" ");
        }
        return finalResult;
    }


    private static boolean isInterSecting(List<Circle> circleList, Circle c) {
        if(circleList == null) return false;
        return circleList.stream().anyMatch(cTemp -> interSectingCircle(c, cTemp));
    }

    private static boolean interSectingCircle(Circle c, Circle cTemp) {
        double distanceBtweenCircle = Math.sqrt((c.x - cTemp.x)*(c.x - cTemp.x) + (c.y - cTemp.y)*(c.y - cTemp.y));
        double totalRad = c.r+cTemp.r;
        return (distanceBtweenCircle<totalRad);
    }

    private static List<Circle> getMaxCircleAtEachWhitePoint(boolean[][] pixels) {
        return IntStream.range(0,pixels.length).mapToObj(i -> IntStream.range(0,pixels[0].length).parallel().filter(j -> pixels[i][j]).mapToObj(j -> new Point2D(i, j)))
                .flatMap(p -> p).parallel().map(p -> maxCircleCalc(p.x, p.y, pixels, null)).collect(Collectors.toList());
    }

    private static Circle maxCircleCalc(int x, int y, boolean[][] pixels, List<Circle> avoidC) {
        return new Circle(x, y, getMaxRadius(x, y, pixels, avoidC));
    }

    private static int getMaxRadius(int i, int j, boolean[][] pixels, List<Circle> avoidCircles) {
        if(pixels[i][j] == false)
            return 0;
        int radius = 0;
        while(isAllWhite(pixels, i, j, radius) && !isInterSecting(avoidCircles, new Circle(i, j, radius)))
            radius++;
        return radius;
    }

    private static boolean isAllWhite(boolean[][] pixels, int i, int j, int radius) {
        List<Point2D> peremPoints = getAllPeremeterPixels(i, j, radius, pixels.length, pixels[0].length);
        return peremPoints.stream().filter(p -> !pixels[p.x][p.y]).count() == 0;
    }

    private static List<Point2D> getAllPeremeterPixels(int xc, int yc, int radius, int width, int height) {
        List<Point2D> resultListPoints = new ArrayList<>();

        int x=0,y=radius,d=3-(2*radius);
        eightWaySymmetricPlot(xc,yc,x,y, resultListPoints);
        while(x<=y)
        {
            if(d<=0)
            {
                d=d+(4*x)+6;
            }
            else
            {
                d=d+(4*x)-(4*y)+10;
                y=y-1;
            }
            x=x+1;
            eightWaySymmetricPlot(xc,yc,x,y, resultListPoints);
        }

        return resultListPoints.stream().filter(p -> withinBoundary(p, width, height)).collect(Collectors.toSet()).stream().toList();
    }

    private static boolean withinBoundary(Point2D p, int width, int height) {
        return (p.x>=0 && p.x <width) && (p.y>=0 && p.y <height);
    }

    private static void eightWaySymmetricPlot(int xc, int yc, int x, int y, List<Point2D> point2DS)
    {
        point2DS.add(new Point2D(x+xc,y+yc));
        point2DS.add(new Point2D(x+xc,-y+yc));
        point2DS.add(new Point2D(x+xc,-y+yc));
        point2DS.add(new Point2D(-x+xc,y+yc));
        point2DS.add(new Point2D(y+xc,x+yc));
        point2DS.add(new Point2D(y+xc,-x+yc));
        point2DS.add(new Point2D(-y+xc,-x+yc));
        point2DS.add(new Point2D(-y+xc,x+yc));
    }
    static int black=0,white=0;
    private static boolean isWhite(int rgb) {
        Color color = new Color(rgb);
        boolean boo = (color.getRed() + color.getGreen() + color.getBlue())/3 > 255.0/2.0;
        if(!boo) black++;
        else white++;
        return boo;
    }

}
