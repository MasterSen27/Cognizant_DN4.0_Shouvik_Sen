package com.example.factory;

public class PdfDocument implements Document 
{
    public void open() 
    {
        System.out.println("Opening PDF Document...");
    }
}
