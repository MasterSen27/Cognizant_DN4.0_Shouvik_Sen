package com.example.factory;

public class WordFactory extends DocumentFactory 
{
    public Document createDocument() 
    {
        return new WordDocument();
    }
}
