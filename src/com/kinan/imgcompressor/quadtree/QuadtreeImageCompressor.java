package com.kinan.imgcompressor.quadtree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class QuadtreeImageCompressor {
    private QuadtreeNode root;
    private int width, height, minimumBlockSize, maxDepth;
    private float compressionTarget;
    private double errorThreshold;
    private String formatName;

    public QuadtreeImageCompressor(BufferedImage image, int minimumBlockSize, float compressionTarget, double errorThreshold, int method, String formatName)
    {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.minimumBlockSize = minimumBlockSize;
        this.maxDepth = 0;
        this.compressionTarget = compressionTarget;
        this.root = new QuadtreeNode(image, 0, 0, width, height, 0, method);
        this.formatName = formatName;
        if (compressionTarget == 0)
        {
            this.errorThreshold = errorThreshold;
        }

        buildTree(image, root);
    }

    public void setErrorThreshold(double threshold) 
    {
        this.errorThreshold = threshold;
    }

    public int getMaxDepth()
    {
        return maxDepth;
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

    public void renderAtDepth(int depth) 
    {
        if (depth > maxDepth) 
        {
            throw new IllegalArgumentException("Depth exceeds max tree depth.");
        }
        BufferedImage image = createImageFromDepth(depth);
        try {
            ImageIO.write(image, this.formatName, new File("quadtree_depth_" + depth + "." + formatName));
            System.out.println("Saved image at depth " + depth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}