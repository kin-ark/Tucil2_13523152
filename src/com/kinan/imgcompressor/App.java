package com.kinan.imgcompressor;

import com.kinan.imgcompressor.io.*;
import com.kinan.imgcompressor.quadtree.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class App {
    public static void main(String[] args) {
        InputHandler input = new InputHandler();
        input.readInputs();

        String inputPath = input.getInputImagePath();
        String outputPath = input.getOutputImagePath();
        String gifPath = input.getOutputGifPath();
        int method = input.getErrorMethod();
        double threshold = input.getErrorThreshold();
        int minBlock = input.getMinimumBlockSize();
        float compressionTarget = input.getCompressionTarget();
        String fileType = input.getFileType();
        long originalSize = new File(inputPath).length();

        // Debug output
        // System.out.println("\n=== Input Diterima ===");
        // System.out.println("Input Image : " + inputPath);
        // System.out.println("Metode Error : " + method);
        // System.out.println("Ambang Error : " + threshold);
        // System.out.println("Blok Minimum : " + minBlock);
        // System.out.println("Target Kompresi : " + compressionTarget);
        // System.out.println("Output Gambar : " + outputPath);
        // System.out.println("Output GIF : " + (gifPath != null ? gifPath : "Tidak digunakan"));
        // System.out.println("File Type : " + fileType);

        long startTime = System.nanoTime();

        try {
            BufferedImage image = ImageIO.read(new File(inputPath));

            // Build compressor
            QuadtreeImageCompressor compressor = new QuadtreeImageCompressor(
                image, minBlock, compressionTarget, threshold, method, fileType
            );

            // Handle output
            OutputHandler outputHandler = new OutputHandler();

            if (gifPath != null && !gifPath.isBlank()) {
                outputHandler.renderGIF(compressor, gifPath, 1000);
            }

            outputHandler.renderAtDepth(compressor, compressor.getMaxDepth(), outputPath);

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1000000;
            System.out.println(executionTime + "ms");

            long compressedSize = new File(outputPath).length();
            double compressRatio = 1 - ((double) compressedSize / originalSize);
            
            System.out.println("Original Size: " + originalSize/1024 + "KB");
            System.out.println("Compressed Size: " + compressedSize/1024 + "KB");
            System.out.println("Compression Ratio: " + compressRatio);
            System.out.println("Tree Depth: " + compressor.getMaxDepth() + 1);
            System.out.println("Number of Nodes: " + compressor.getNumOfNodes());
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }
}

