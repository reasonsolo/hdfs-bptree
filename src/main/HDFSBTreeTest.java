package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.List;

import org.junit.Before;

import trees.BPlusTree;
import util.BattingBPlusConverter;
import util.BattingBPlusRecord;
import util.BPlusRecord;
import util.BPlusConverter;
import util.Converter;

import org.apache.hadoop.conf.Configuration;

public class HDFSBTreeTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException{
        if (args.length < 2) {
            System.err.println("not enough arguments");
            System.exit(-1);
        }
        Configuration conf = new Configuration();
        BPlusTree<String, String, BPlusRecord<String, String>> tree = 
                new BPlusTree<String, String, BPlusRecord<String, String>>(
                        7, 
                        new BPlusConverter(), 
                        null, 
                        args[0],
                        args[1],
                        null);
        for (int i = 0; i < 100; ++i) {
            tree.insert(new BPlusRecord<String, String>("a" + i, "" + i));
       }
        for (int i = 0; i < 100; ++i) {
            tree.insert(new BPlusRecord<String, String>("a" + i, "" + (i + 1)));
        }
        tree.printTree();
        
        tree.syncToHdfs(args[1], conf);
        File deleteit = new File(args[0]);
        deleteit.delete();
        System.out.println();
        for (int i = 0; i < 100; ++i) {
            System.out.println("search a" + i + " " + tree.search("a" + i));
        }
        for (int i = 0; i < 100; ++i) {
            List<Long> results = tree.getRange("a" + i, "a" + i);
            System.out.print("a" + i + ": ");
            for (Long result: results) {
                System.out.print(" " + result);
            }
            System.out.println();
        }
        
        
    }
}
