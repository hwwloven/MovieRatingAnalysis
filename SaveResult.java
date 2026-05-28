import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

public class SaveResult {
    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        Table resultTable = conn.getTable(TableName.valueOf("movie_analysis_result"));

        // 将步骤一中的统计结果手动写入（示例数据，请替换为实际统计值）
        String[][] data = {
            {"1994", "7"},
            {"1995", "4"},
            {"1996", "5"},
            {"1997", "6"},
            {"1998", "4"},
            {"1999", "5"},
            {"2000", "3"},
            {"2001", "4"},
            {"2002", "5"},
            {"2003", "4"}
        };

        for (String[] row : data) {
            String rowkey = "year|" + row[0];
            Put put = new Put(Bytes.toBytes(rowkey));
            put.addColumn(Bytes.toBytes("stat"), Bytes.toBytes("count"), Bytes.toBytes(row[1]));
            resultTable.put(put);
            System.out.println("写入: year|" + row[0] + " -> " + row[1] + " 部");
        }

        resultTable.close();
        conn.close();
        System.out.println("结果写入完成！");
    }
}
