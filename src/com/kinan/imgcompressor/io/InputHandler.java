package com.kinan.imgcompressor.io;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

public class InputHandler {
    private String inputImagePath;
    private int errorMethod;
    private double errorThreshold;
    private int minimumBlockSize;
    private float compressionTarget;
    private String outputImagePath;
    private String outputGifPath;


    public void readInputs() {
        Scanner scanner = new Scanner(System.in);
        
        try 
        {
            // Input Image Path
            System.out.print("Masukkan alamat absolut gambar yang akan dikompresi: ");
            inputImagePath = scanner.nextLine().trim();
            if (inputImagePath.isEmpty()) throw new IllegalArgumentException("Path gambar tidak boleh kosong.");

            // Error Calculation Method
            System.out.print("Pilih metode perhitungan error (1: Variance, 2: Mean Absolute Difference, 3: Max Pixel Difference, 4 Entropy): ");
            errorMethod = scanner.nextInt();
            if (errorMethod < 1 || errorMethod > 4) throw new IllegalArgumentException("Metode error tidak valid.");

            // Error Threshold
            System.out.print("Masukkan ambang batas error: ");
            errorThreshold = scanner.nextDouble();
            if (errorThreshold < 0) throw new IllegalArgumentException("Ambang batas error harus positif."); // temp

            // Minimum Block Size
            System.out.print("Masukkan ukuran blok minimum: ");
            minimumBlockSize = scanner.nextInt();
            if (minimumBlockSize <= 0) throw new IllegalArgumentException("Ukuran blok minimum harus lebih dari 0.");

            // Compression Target Percentage
            System.out.print("Masukkan target persentase kompresi (1.0 = 100%, 0 untuk menonaktifkan): ");
            compressionTarget = scanner.nextFloat();
            if (compressionTarget < 0 || compressionTarget > 1) throw new IllegalArgumentException("Target kompresi harus antara 0 dan 1.");

            // Output Image Path
            scanner.nextLine();
            System.out.print("Masukkan alamat absolut gambar hasil kompresi: ");
            outputImagePath = scanner.nextLine().trim();
            if (outputImagePath.isEmpty()) throw new IllegalArgumentException("Path output gambar tidak boleh kosong.");

            // Output GIF Path
            System.out.print("Masukkan alamat absolut gif (opsional, tekan enter untuk melewati): ");
            outputGifPath = scanner.nextLine().trim();
            if (outputGifPath.isEmpty()) {
                outputGifPath = null;
            }
        } catch (InputMismatchException e) {
            System.err.println("Input tidak valid. Pastikan Anda memasukkan angka dengan format yang benar.");
            scanner.nextLine();
            readInputs();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            readInputs();
        } finally {
            scanner.close();
        }
    }

    public String getInputImagePath() 
    {
        return inputImagePath;
    }

    public int getErrorMethod() 
    {
        return errorMethod;
    }

    public double getErrorThreshold() 
    {
        return errorThreshold;
    }

    public int getMinimumBlockSize() 
    {
        return minimumBlockSize;
    }

    public float getCompressionTarget() 
    {
        return compressionTarget;
    }

    public String getOutputImagePath() 
    {
        return outputImagePath;
    }

    public String getOutputGifPath() 
    {
        return outputGifPath;
    }

    public String getFileType()
    {
        String format = "";
        File file = new File(inputImagePath);
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            format = name.substring(lastDot + 1).toLowerCase();
        }
        return format;
    }
}

