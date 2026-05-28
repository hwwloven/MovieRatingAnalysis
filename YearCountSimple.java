import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YearCountSimple {
    public static void main(String[] args) throws Exception {
        // 连接 HBase
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);

        Table rawTable = conn.getTable(TableName.valueOf("douban_movie_raw"));
        Table resultTable = conn.getTable(TableName.valueOf("movie_analysis_result"));

        // 统计每年电影数量
        Map<String, Integer> yearCount = new HashMap<>();

        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("year"));

        ResultScanner scanner = rawTable.getScanner(scan);
        for (Result result : scanner) {
            byte[] yearBytes = result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("year"));
            if (yearBytes != null) {
                // 年份字段可能包含一些非数字字符，这里简单过滤一下
                String year = Bytes.toString(yearBytes).replaceAll("[^0-9]", "");
                if (!year.isEmpty()) {
                    yearCount.put(year, yearCount.getOrDefault(year, 0) + 1);
                }
            }
        }
        scanner.close();

        // 将结果写入 movie_analysis_result 表
        for (String year : yearCount.keySet()) {
            String rowkey = "year|" + year;
            Put put = new Put(Bytes.toBytes(rowkey));
            put.addColumn(Bytes.toBytes("stat"), Bytes.toBytes("count"),
                    Bytes.toBytes(String.valueOf(yearCount.get(year))));
            resultTable.put(put);
        }

        rawTable.close();
        resultTable.close();
        conn.close();
        System.out.println("统计完成，结果已写入 movie_analysis_result 表。");
    }
}
