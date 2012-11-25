package trees;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.conf.Configuration;

public class HdfsFile {
    
    public HdfsFile(String filename, Configuration conf) throws IOException {
        fs_ = FileSystem.get(conf);
        path_ = new Path(filename);
        open();
    }
    
    private void open() throws IOException {
        if (fs_.exists(path_)) {
            throw new IOException("File already exists!");
        }
        out_ = fs_.create(path_);
        in_ = fs_.open(path_);
    }
    
    public void write(byte[] buffer, int offset, int length) throws IOException {
        out_.write(buffer, offset, length);
    }
    
    public void copyLocalToHdfs(String src) throws IOException {
        Path srcPath = new Path(src);
        fs_.copyFromLocalFile(true, srcPath, path_);
    }
    
    public void read(long position, byte[] buffer, int offset, int length) throws IOException {
        in_.read(position, buffer, offset, length);
    }
    
    private FileSystem fs_;
    private Path path_;
    private FSDataInputStream in_;
    private FSDataOutputStream out_;
}
