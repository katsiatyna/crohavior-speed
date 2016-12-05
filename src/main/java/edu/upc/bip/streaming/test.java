package edu.upc.bip.streaming;

import java.io.File;
import java.io.IOException;

/**
 * Created by osboxes on 16/11/16.
 */
public class test {

    public static void main(String[] args)
    {
        String fileName = "/home/osboxes/test/" +22;
        File f = new File(fileName);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
