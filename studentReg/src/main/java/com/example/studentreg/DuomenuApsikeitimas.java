package com.example.studentreg;
import java.io.File;
import java.io.IOException;

public interface DuomenuApsikeitimas {
    void eksportuoti(File file) throws IOException;
    void importuoti(File file) throws IOException;
    String getSupportedFileExtensions();
}