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
        String fileType = input.getFileType(inputPath);
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
                image, minBlock, compressionTarget, threshold, method, fileType, originalSize / 1024
            );

            // Handle output
            OutputHandler outputHandler = new OutputHandler();

            System.err.println("");
            System.out.println("Kompresi Selesai!");

            outputHandler.renderAtDepth(compressor, compressor.getMaxDepth(), outputPath);


            if (gifPath != null && !gifPath.isBlank()) {
                outputHandler.renderGIF(compressor, gifPath, 1000);
            }

            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1000000;
            System.out.println("Lama Eksekusi: " + executionTime + "ms");

            long compressedSize = new File(outputPath).length();
            double compressRatio = 1 - ((double) compressedSize / originalSize);
            
            System.out.println("Size Sebelum Dikompres: " + originalSize/1024 + "KB");
            System.out.println("Size Setelah Dikompres: " + compressedSize/1024 + "KB");
            System.out.printf("Persentase Kompresi: %.2f%%\n", compressRatio * 100);
            System.out.println("Kedalaman Pohon: " + compressor.getMaxDepth());
            System.out.println("Jumlah Simpul: " + compressor.getNumOfNodes());
        } catch (IOException e) {
            System.err.println("Error memuat gambar: " + e.getMessage());
        }
    }
}

