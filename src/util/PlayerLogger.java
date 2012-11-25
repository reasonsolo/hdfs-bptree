package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PlayerLogger extends BufferedWriter
{
    public PlayerLogger(String filePath) throws IOException
    {
        super(new FileWriter(filePath));
    }
}
