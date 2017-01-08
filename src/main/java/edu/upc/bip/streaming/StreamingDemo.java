package edu.upc.bip.streaming;


import edu.upc.bip.model.Coordinate;
import edu.upc.bip.model.EsRecord;
import edu.upc.bip.model.Transaction;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.spark.streaming.api.java.JavaEsSparkStreaming;
import scala.Tuple2;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by osboxes on 16/11/16.
 */
public class StreamingDemo {

    private static final Pattern SPACE = Pattern.compile(",");
    private static final Integer LAT = 0;
    private static final Integer LONG = 1;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        //System.setProperty("hadoop.home.dir", "c:\\hadoop-2.7.3\\");
        SparkConf conf = new SparkConf().setAppName("StreamingTest").setMaster("local[*]").set("spark.executor.memory","12g").set("spark.driver.maxResultSize","2g");

        JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(5000));

        Set<String> topicsSet = new HashSet<>(Arrays.asList("plt-input"));
        Map<String, String> kafkaParams = new HashMap<>();
        kafkaParams.put("metadata.broker.list", "localhost:9092");

        // Create direct kafka stream with brokers and topics
        JavaPairInputDStream<String, String> directKafkaStream =
                KafkaUtils.createDirectStream(ssc,String.class,String.class, StringDecoder.class,
                        StringDecoder.class,kafkaParams,topicsSet);


        // Get the lines, split them into words, count the words and print
        JavaDStream<String> lines = directKafkaStream.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) {
                return tuple2._2();
            }
        });

        //lines to Transactions
        JavaDStream<Transaction> transactions = lines.map(
                new Function<String, Transaction>() {
                    @Override
                    public Transaction call(String row) throws Exception {
                        String[] words = row.split(";");
                        Double latitude = (double)(5 * Math.round(Double.parseDouble(words[LAT]) * 10000) / 5) / 10000;
                        Double longitude = (double)(5 * Math.round(Double.parseDouble(words[LONG]) * 10000) / 5) / 10000;
                        Coordinate coordinate = new Coordinate(latitude, longitude);
                        return new Transaction(coordinate);
                    }
                }
        );

        JavaPairDStream<Coordinate, Integer> pairs = transactions.mapToPair(
                new PairFunction<Transaction, Coordinate, Integer>() {
                    @Override
                    public Tuple2<Coordinate, Integer> call(Transaction transaction) throws Exception {
                        return new Tuple2<Coordinate, Integer>(transaction.getCoordinate(), 1);
                    }
                }
        );

        JavaPairDStream<Coordinate, Integer> counts5Sec = pairs.reduceByKey(
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer a, Integer b) throws Exception {
                        return a + b;
                    }
                }
        );

        JavaPairDStream<Coordinate, Integer> counts10Sec = pairs.reduceByKeyAndWindow(
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer a, Integer b) throws Exception {
                        return a + b;
                    }
                },
        new Duration(10000), new Duration(10000));

        JavaPairDStream<Coordinate, Integer> counts15Sec = pairs.reduceByKeyAndWindow(
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer a, Integer b) throws Exception {
                        return a + b;
                    }
                },
                new Duration(15000), new Duration(15000));

        writeJson(counts5Sec,objectMapper, "5");
        writeJson(counts10Sec,objectMapper, "10");
        writeJson(counts15Sec,objectMapper, "15");

        // Start the computation
        ssc.start();
        ssc.awaitTermination();
    }

    private static void writeJson(JavaPairDStream<Coordinate, Integer> tuples, ObjectMapper objectMapper, String indexType) throws IOException {

       JavaDStream<EsRecord> transformed =  tuples.transform(new Function2<JavaPairRDD<Coordinate, Integer>, Time, JavaRDD<EsRecord>>() {
           @Override
           public JavaRDD<EsRecord> call(JavaPairRDD<Coordinate, Integer> coordinateIntegerJavaPairRDD, Time time) throws Exception {
               //OutputStream outputStream = new FileOutputStream(fileName);
               JavaRDD<EsRecord> gridBoxes = coordinateIntegerJavaPairRDD.map(
                       new Function<Tuple2<Coordinate, Integer>, EsRecord>() {
                           @Override
                           public EsRecord call(Tuple2<Coordinate, Integer> tuple) throws Exception {
                               Coordinate coordinate = tuple._1();
                               EsRecord gridBox = new EsRecord();
                               gridBox.setType(indexType);
                               gridBox.setRdd(coordinateIntegerJavaPairRDD.id());
                               gridBox.setTs(time.milliseconds());
                               //gridBox.put()
                               gridBox.setA(coordinate.getLatitude());
                               gridBox.setO(coordinate.getLongitude());
                               gridBox.setC(tuple._2());
                               return gridBox;
                           }
                       }
               );
               return gridBoxes;
           }
       });
        JavaEsSparkStreaming.saveToEs(transformed, "heatmap/{type}");
      /*  tuples.foreachRDD(new VoidFunction2<JavaPairRDD<Coordinate, Integer>, Time>() {
            @Override
            public void call(JavaPairRDD<Coordinate, Integer> coordinateIntegerJavaPairRDD, Time time) throws Exception {

                //String fileName = "/home/osboxes/test/" +time.toString().split(" ")[0];
                //File f = new File(fileName);
                //f.createNewFile();
                //OutputStream outputStream = new FileOutputStream(fileName);
                JavaRDD<Map<String, Object>> gridBoxes = coordinateIntegerJavaPairRDD.map(
                        new Function<Tuple2<Coordinate, Integer>, Map<String, Object>>() {
                            @Override
                            public Map<String, Object> call(Tuple2<Coordinate, Integer> tuple) throws Exception {
                                Coordinate coordinate = tuple._1();
                                Map<String, Object> gridBox = new HashMap<>();
                                gridBox.put("rdd", coordinateIntegerJavaPairRDD.id());
                                gridBox.put("ts", time.toString().split(" ")[0]);
                                //gridBox.put()
                                gridBox.put("latitude", coordinate.getA());
                                gridBox.put("longitude", coordinate.getO());
                                gridBox.put("count", tuple._2());
                                return gridBox;
                            }
                        }
                );
                JavaEsSpark.saveToEs(gridBoxes, "spark/3s");
                //Map<String, Object> data = new HashMap<>();
                //data.put("data", gridBoxes);
                //objectMapper.writeValue(outputStream, data);
                //outputStream.close();
            }
        });
*/

    }

}
