package trees;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.conf.Configuration;

public class HdfsFile {
    
    public HdfsFile(String filename, Configuration conf) throws IOException {
        fs_ = FileSystem.get(conf);
        path_ = new Path(filename);
    }
    
    public void open() throws IOException {
        //out_ = fs_.append(path_);
        in_ = fs_.open(path_);
        status_ = fs_.getFileStatus(path_);
    }
    
    public void write(byte[] buffer, int offset, int length) throws IOException {
        //out_.write(buffer, offset, length);
    }
    
    public void copyLocalToHdfs(String src) throws IOException {
        if (fs_.exists(path_)) {
            open();
            return;
        }
        Path srcPath = new Path(src);
//        System.err.println("moving " + srcPath.toString() + " to " + path_.toString());
        fs_.copyFromLocalFile(true, srcPath, path_);
        System.err.println("synchronized");
//        fs_.rename(srcPath, path_);
        open();
    }
    
    public void read(long position, byte[] buffer, int offset, int length) throws IOException {
        if (position < 0) {
            position = status_.getLen() + position;
        }
        in_.read(position, buffer, offset, length);
//        System.err.println(new String(buffer, "ascii") + " " + length + " "
//                + buffer.length + " " + in_.getPos());
    }
    
    private FileSystem fs_;
    private Path path_;
    private FSDataInputStream in_;
    private FileStatus status_;
    //private FSDataOutputStream out_;
}
