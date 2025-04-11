package com.kinan.imgcompressor.quadtree;

import java.awt.image.BufferedImage;

public class QuadtreeNode {
    private final int x, y, width, height, depth;
    private final double error;
    private QuadtreeNode[] children;
    private boolean isLeaf;
    private final int avgColor;
    private final int method;

    public QuadtreeNode(BufferedImage image, int x, int y, int width, int height, int depth, int method)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.isLeaf = false;
        this.children = null;
        this.method = method;

        this.avgColor = computeAvgColor(image, x, y, width, height);
        this.error = computeError(image, x, y, width, height, method);
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

    public void setLeafTrue()
    {
        isLeaf = true;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public QuadtreeNode[] getChildren()
    {
        return children;
    }

    public double getError()
    {
        return error;
    }

    public int getDepth()
    {
        return depth;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getArea()
    {
        return width * height;
    }

    public int getAvgColor()
    {
        return avgColor;
    }

    public boolean split(BufferedImage image)
    {
        if (width <= 1 || height <= 1)
        {
            isLeaf = true;
            return false;
        }
    
        int halfW = width / 2;
        int halfH = height / 2;
        int remW = width - halfW;
        int remH = height - halfH;
    
        children = new QuadtreeNode[4];
        children[0] = new QuadtreeNode(image, x, y, halfW, halfH, depth + 1, method);
        children[1] = new QuadtreeNode(image, x + halfW, y, remW, halfH, depth + 1, method);
        children[2] = new QuadtreeNode(image, x, y + halfH, halfW, remH, depth + 1, method);
        children[3] = new QuadtreeNode(image, x + halfW, y + halfH, remW, remH, depth + 1, method);
        return true;
    }
    

    private int computeAvgColor(BufferedImage image, int x, int y, int width, int height)
    {
        long r = 0, g = 0, b = 0;
        int area = width * height;

        for (int i = x; i < x + width; i++)
        {
            for (int j = y; j < y + height; j++)
            {
                int pixel = image.getRGB(i, j);
                r += (pixel >> 16) & 0xFF;
                g += (pixel >> 8) & 0xFF;
                b += pixel & 0xFF;
            }
        }

        int avgR = (int) (r/area);
        int avgG = (int) (g/area);
        int avgB = (int) (b/area);
        return (avgR << 16) | (avgG << 8) | avgB;
    }

    private double computeError(BufferedImage img, int x, int y, int w, int h, int method) {
        switch (method) {
            case 1:
                return computeVarianceError(img, x, y, h, w);
            case 2:
                return computeMADError(img, x, y, h, w);
            case 3:
                return computeMPDError(img, x, y, h, w);
            case 4:
                return computeEntropyError(img, x, y, h, w);
            case 5:
                return computeSSIMError(img, x, y, h, w);
            default:
                throw new IllegalArgumentException("Invalid error method: " + method);
        }
    }

    private double computeVarianceError(BufferedImage image, int x, int y, int height, int width)
    {
        int avgR = (avgColor >> 16) & 0xFF;
        int avgG = (avgColor >> 8) & 0xFF;
        int avgB = (avgColor) & 0xFF;
    
        int area = height * width;
    
        double errorR = 0;
        double errorG = 0;
        double errorB = 0;
        
        int maxX = Math.min(x + width, image.getWidth());
        int maxY = Math.min(y + height, image.getHeight());
    
        for (int i = x; i < maxX; i++)
        {
            for (int j = y; j < maxY; j++)
            {
                int pixel = image.getRGB(i, j);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel) & 0xFF;
    
                errorR += Math.pow((r - avgR), 2);
                errorG += Math.pow((g - avgG), 2);
                errorB += Math.pow((b - avgB), 2);
            }
        }
    
        errorR /= area;
        errorG /= area;
        errorB /= area;
        
        return ((errorR + errorG + errorB) / 3);
    }

    private double computeMADError(BufferedImage image, int x, int y, int height, int width)
    {
        int avgR = (avgColor >> 16) & 0xFF;
        int avgG = (avgColor >> 8) & 0xFF;
        int avgB = (avgColor) & 0xFF;

        int area = height * width;
    
        double errorR = 0;
        double errorG = 0;
        double errorB = 0;
        
        int maxX = Math.min(x + width, image.getWidth());
        int maxY = Math.min(y + height, image.getHeight());
    
        for (int i = x; i < maxX; i++)
        {
            for (int j = y; j < maxY; j++)
            {
                int pixel = image.getRGB(i, j);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel) & 0xFF;
    
                errorR += Math.abs((r - avgR));
                errorG += Math.abs((g - avgG));
                errorB += Math.abs((b - avgB));
            }
        }

        errorR /= area;
        errorG /= area;
        errorB /= area;
        
        return ((errorR + errorG + errorB) / 3);
    }

    private double computeMPDError(BufferedImage image, int x, int y, int height, int width) {
        int minR = 255, minG = 255, minB = 255;
        int maxR = 0, maxG = 0, maxB = 0;
    
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
    
                if (r < minR) minR = r;
                if (g < minG) minG = g;
                if (b < minB) minB = b;
    
                if (r > maxR) maxR = r;
                if (g > maxG) maxG = g;
                if (b > maxB) maxB = b;
            }
        }
    
        int diffR = maxR - minR;
        int diffG = maxG - minG;
        int diffB = maxB - minB;
    
        return ((diffR + diffG + diffB) / 3);
    }

    private double computeEntropyError(BufferedImage image, int x, int y, int height, int width)
    {
        int[] red = new int[256];
        int[] green = new int[256];
        int[] blue = new int[256];
        int totalPixels = width * height;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                int rgb = image.getRGB(j, i);
                int ri = (rgb >> 16) & 0xFF;
                int gi = (rgb >> 8) & 0xFF;
                int bi = rgb & 0xFF;
                
                red[ri]++;
                green[gi]++;
                blue[bi]++;
            }
        }

        double redEntropy = computeEntropy(red, totalPixels);
        double greenEntropy = computeEntropy(green, totalPixels);
        double blueEntropy = computeEntropy(blue, totalPixels);

        return ((redEntropy + greenEntropy + blueEntropy) / 3);
    }

    private double computeEntropy(int[] color, int totalPixels)
    {
        double entropy = 0;

        for (int i = 0; i < 256; i++)
        {
            if (color[i] > 0)
            {
                double p = (double) color[i] / totalPixels;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        return entropy;
    }

    private double computeSSIMError(BufferedImage image, int x, int y, int height, int width)
    {
        int avgR = (avgColor >> 16) & 0xFF;
        int avgG = (avgColor >> 8) & 0xFF;
        int avgB = (avgColor) & 0xFF;

        double C2 = Math.pow(0.03 * 255, 2);

        int n = width * height;
        double varR = 0, varG = 0, varB = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                varR += Math.pow((r - avgR), 2);
                varG += Math.pow((g - avgG), 2);
                varB += Math.pow((b - avgB), 2);
            }
        }

        varR /= n;
        varG /= n;
        varB /= n;

        // SSIM (varY = 0, covar = 0)
        double ssimR = (C2) / (varR + C2);

        double ssimG = (C2) / (varG + C2);

        double ssimB = (C2) / (varB + C2);

        double ssim = (ssimR + ssimG + ssimB) / 3.0;

        return 1 - ssim;
    }
}
