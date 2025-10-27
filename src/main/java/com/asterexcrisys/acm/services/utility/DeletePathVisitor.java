package com.asterexcrisys.acm.services.utility;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@SuppressWarnings("unused")
public class DeletePathVisitor extends SimpleFileVisitor<Path> {

    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
        Files.delete(directory);
        return FileVisitResult.CONTINUE;
    }

}