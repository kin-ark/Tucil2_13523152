package com.kinan.imgcompressor.quadtree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class QuadtreeImageCompressor {
    private static int numOfNodes = 1;
    private QuadtreeNode root;
    private int width, height, minimumBlockSize, maxDepth;
    private float compressionTarget;
    private double errorThreshold;
    private String formatName;
    private long originalSize;

    public QuadtreeImageCompressor(BufferedImage image, int minimumBlockSize, float compressionTarget, double errorThreshold, int method, String formatName, long originalSize)
    {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.minimumBlockSize = minimumBlockSize;
        this.maxDepth = 0;
        this.compressionTarget = compressionTarget;
        this.originalSize = originalSize;
        this.root = new QuadtreeNode(image, 0, 0, width, height, 0, method);
        this.formatName = formatName;
        if (compressionTarget == 0)
        {
            this.errorThreshold = errorThreshold;
            buildTree(image, root);
        }
        else
        {
            this.minimumBlockSize = 1;
            compressToTargetSize(image, (double) originalSize * (1 - compressionTarget), method);
        }
    }

    public void setErrorThreshold(double threshold) 
    {
        this.errorThreshold = threshold;
    }

    public int getMaxDepth()
    {
        return maxDepth;
    }

    public int getNumOfNodes()
    {
        return numOfNodes;
    }

    public String getFormatName()
    {
        return formatName;
    }

    private void buildTree(BufferedImage image, QuadtreeNode node)
    {
        if (node.getError() <= errorThreshold || node.getArea()/4 < minimumBlockSize)
        {
            if (node.getDepth() > this.maxDepth)
            {
                this.maxDepth = node.getDepth();
            }
            node.setLeafTrue();
            return;
        }

        if (node.split(image))
        {
            numOfNodes += 4;
            for (QuadtreeNode child : node.getChildren()) 
            {
                buildTree(image, child);
            }
        }
    }

    public List<QuadtreeNode> getLeafNodes(int depth)
    {
        if (depth > maxDepth)
        {
            throw new IllegalArgumentException("Depth > max tree depth.");
        }

        List<QuadtreeNode> leafNodes = new ArrayList<>();
        collectLeafNodes(root, depth, leafNodes);
        return leafNodes;
    }

    private void collectLeafNodes(QuadtreeNode node, int depth, List<QuadtreeNode> leafNodes)
    {
        if (node.isLeaf() || node.getDepth() == depth)
        {
            leafNodes.add(node);
        }
        else if (node.getChildren() != null)
        {
            for (QuadtreeNode child: node.getChildren())
            {
                collectLeafNodes(child, depth, leafNodes);
            }
        }
    }

    public BufferedImage createImageFromDepth(int depth) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        List<QuadtreeNode> leafNodes = getLeafNodes(depth);
        for (QuadtreeNode node : leafNodes) {
            g.setColor(new Color(node.getAvgColor()));
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
        }

        g.dispose();
        return image;
    }

    public void compressToTargetSize(BufferedImage image, double targetSizeKB, int method) {
        double low, high;
        switch (method) {
            case 1:
                low = 0;
                high = 16256.5;
                break;
            case 2:
                low = 0;
                high = 127.5;
                break;
            case 3:
                low = 0;
                high = 255;
                break;
            case 4:
                low = 0;
                high = 8;
                break;
            case 5:
                low = 0;
                high = 1;
                break;
            default:
                throw new AssertionError();
        }

        double bestThreshold = high;
        BufferedImage bestImage = null;
    
        int iteration = 0;
        double tolerance = 0.01;
    
        while (iteration < 100 && (high - low) > 0.0001) {
            iteration++;
            double mid = (low + high) / 2.0;
    
            setErrorThreshold(mid);
            this.root = new QuadtreeNode(image, 0, 0, width, height, 0, method);
            this.maxDepth = 0;
            buildTree(image, root);
            BufferedImage result = createImageFromDepth(this.maxDepth);
            double sizeKB = estimateImageSize(result);
    
            double diff = Math.abs(sizeKB - targetSizeKB);
            if (diff / targetSizeKB <= tolerance) {
                bestThreshold = mid;
                bestImage = result;
                // System.out.printf("Selesai di iterasi %d\n", iteration);
                break;
            }
    
            if (sizeKB > targetSizeKB) {
                low = mid;
            } else {
                bestThreshold = mid;
                bestImage = result;
                high = mid;
            }
    
            // System.out.printf("Iteration %d -> Threshold: %.4f | Size: %.2f KB | Target: %.2f KB\n", iteration, mid, sizeKB, targetSizeKB);
        }
    
        System.out.printf("\nSelected threshold: %.4f after %d iterations\n", bestThreshold, iteration);
    }
    
    private double estimateImageSize(BufferedImage image)
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ImageIO.write(image, formatName, baos);
        return baos.size() / 1024.0;
        } catch (IOException e) {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
    }
}