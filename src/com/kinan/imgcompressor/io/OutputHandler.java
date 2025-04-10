package com.kinan.imgcompressor.io;

import com.kinan.imgcompressor.quadtree.*;
import com.github.dragon66.AnimatedGIFWriter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class OutputHandler {

    public void renderAtDepth(QuadtreeImageCompressor compressor, int depth, String outputPath) 
    {
        try {
            BufferedImage image = compressor.createImageFromDepth(depth);
            String formatName = compressor.getFormatName();
            ImageIO.write(image, formatName, new File(outputPath));
            System.out.println("Compressed Image Path: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to save image at depth " + depth);
            e.printStackTrace();
        }
    }

    public void renderGIF(QuadtreeImageCompressor compressor, String outputGifPath, int delayMs) 
    {
        try (OutputStream fos = new FileOutputStream(outputGifPath)) {
            AnimatedGIFWriter gifWriter = new AnimatedGIFWriter(true);
            gifWriter.prepareForWrite(fos, -1, -1);
            for (int depth = 0; depth <= compressor.getMaxDepth(); depth++) {
                BufferedImage frame = compressor.createImageFromDepth(depth);
                gifWriter.writeFrame(fos, frame, delayMs);
            }
            System.out.println("Saved GIF animation to: " + outputGifPath);
        } catch (Exception e) {
            System.err.println("Failed to save GIF animation.");
            e.printStackTrace();
        }
    }
} 

