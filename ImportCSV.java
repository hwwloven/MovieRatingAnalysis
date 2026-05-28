import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import java.io.*;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import java.net.URI;
public class ImportCSV {
    public static void main(String[] args) throws Exception {
        // 连接 HBase
        Configuration hbaseConf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(hbaseConf);
        Table table = connection.getTable(TableName.valueOf("douban_movie_raw"));

        // 读取 HDFS 文件
        Configuration hdfsConf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create("hdfs://localhost:9000"), hdfsConf);

        // 如果有无表头的文件，这里改为 /input_data/douban_noheader.csv
        Path path = new Path("hdfs://localhost:9000/input_data/douban.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path), "UTF-8"));

        String line;
        reader.readLine(); // 跳过表头
        while ((line = reader.readLine()) != null) {
            // 用逗号分割，但评论字段可能包含逗号，所以限制分割数量
            String[] cols = line.split(",", 8);
            if (cols.length < 7) continue;

            String rank = cols[0];
            String name = cols[1];
            String year = cols[2];
            String rating = cols[3];
            // cols[4] 是评价人数，这里暂不导入
            String director = cols[5];
            String genre = cols[6];
            // 评论字段可能很长，截取前100字符
            String comments = cols.length > 7 ? cols[7].substring(0, Math.min(100, cols[7].length())) : "";

            // 用影片名作为行键，如果存在同名电影可自行处理，这里简化为直接覆盖
            Put put = new Put(Bytes.toBytes(name));
            put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("rank"), Bytes.toBytes(rank));
            put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("year"), Bytes.toBytes(year));
            put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("rating"), Bytes.toBytes(rating));
            put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("director"), Bytes.toBytes(director));
            put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("genre"), Bytes.toBytes(genre));
            put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("comments"), Bytes.toBytes(comments));
            table.put(put);
        }
        reader.close();
        table.close();
        connection.close();
        System.out.println("成功导入所有数据！");
    }
}
