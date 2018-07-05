package com.example.filesearch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSearchApp {

    String path;
    String regex;
    String zipFileName;
    Pattern pattern;
    List<File> zipFiles = new ArrayList<>();

    public static void main(String[] args) {
        FileSearchApp app = new FileSearchApp();

        switch (Math.min(args.length, 3)) {
            case 0:
                System.out.println("USAGE: FileSearchApp path [regex] [zipfile]");
                return;
            case 3:
                app.setZipFileName(args[2]);
            case 2:
                app.setRegex(args[1]);
            case 1:
                app.setPath(args[0]);
        }

        try {
            app.walkDirectory(app.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void walkDirectory(String path) throws IOException {
        walkDirectory8(path);
        zipFiles7();
    }

    public void zipFiles7() throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getZipFileName())) ) {
            File baseDir = new File(getPath());

            for (File file : zipFiles) {
                // fileName must be a relative path, not an absolute one.
                String fileName = getRelativeFilename(file, baseDir);

                ZipEntry zipEntry = new ZipEntry(fileName);
                zipEntry.setTime(file.lastModified());
                out.putNextEntry(zipEntry);

                Files.copy(file.toPath(), out);

                out.closeEntry();
            }
        }
    }

    private String getRelativeFilename(File file, File baseDir) {
        String filename = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length());
        filename.replace('\\', '/');

        while (filename.startsWith("/")) {
            filename = filename.substring(1);
        }

        return filename;
    }


    public void walkDirectory6(String path) throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles();

        for(File file : files) {
            if (file.isDirectory()) {
                walkDirectory6(file.getAbsolutePath());
            } else {
                processFile(file);
            }
        }
    }

    public void walkDirectory7(String path) throws IOException {
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                processFile(file.toFile());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void walkDirectory8(String path) throws IOException {
        Files.walk(Paths.get(path))
                .forEach(f -> processFile(f.toFile()));
    }

    private void processFile(File file) {
        try {
            if (searchFile(file)) {
                addFileToZip(file);
            }
        } catch (IOException|UncheckedIOException e) {
            System.out.println("Error Processing file: " + file);
        }

    }

    public boolean searchFile(File file) throws IOException {
        return searchFile8(file);
    }

    public boolean searchFile6(File file) throws FileNotFoundException {
        boolean found = false;
        Scanner scanner = new Scanner(file, "UTF-8");
        while (scanner.hasNextLine()) {
            found = searchText(scanner.nextLine());
            if(found) { break; }
        }
        scanner.close();
        return found;
    }

    public boolean searchFile7(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8); // reads all files to memory at once
        for (String line : lines) {
            if (searchText(line)) {
                return true;
            }
        }
        return false;
    }

    public boolean searchFile8(File file) throws IOException {
        return Files.lines(file.toPath(), StandardCharsets.UTF_8).anyMatch(t -> searchText(t));
    }

    public boolean searchText(String text) {
        return (this.getRegex() == null) ||
         this.pattern.matcher(text).matches(); // matches is exact match. find() for partial matches
    }


    public void addFileToZip(File file) {
        if (getZipFileName() != null) {
            zipFiles.add(file);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }
}
