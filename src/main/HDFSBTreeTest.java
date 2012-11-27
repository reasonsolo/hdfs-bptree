package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

import trees.BPlusTree;
import util.BattingBPlusConverter;
import util.BattingBPlusRecord;

import org.apache.hadoop.conf.Configuration;

public class HDFSBTreeTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException{
        // TODO Auto-generated method stub
        if (args.length < 2) {
            System.err.println("not enough arguments");
            System.exit(-1);
        }
        BPlusTree<String, Long, BattingBPlusRecord> tree = 
                new BPlusTree<String, Long, BattingBPlusRecord>(
                        4, 
                        new BattingBPlusConverter(), 
                        null, 
                        args[0],
                        args[1],
                        null);
        for (int i = 0; i < 10; ++i) {
            tree.insert(new BattingBPlusRecord("a" + i, new Long(i)));
        }
        tree.printTree();
        Configuration conf = new Configuration();
        tree.syncToHdfs(args[1], conf);
        File deleteit = new File(args[0]);
        deleteit.delete();
        System.out.println();
        for (int i = 0; i < 11; ++i) {
            System.out.print("search a" + i + " " + tree.search("a" + i));
            System.out.println();
        }
    }
}
