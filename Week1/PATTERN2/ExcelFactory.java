package com.example.factory;

public class ExcelFactory extends DocumentFactory 
{
    public Document createDocument() 
    {
        return new ExcelDocument();
    }
}
