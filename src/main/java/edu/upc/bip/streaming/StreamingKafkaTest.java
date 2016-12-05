/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package edu.upc.bip.streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;
import kafka.serializer.StringDecoder;

import java.util.*;
import java.util.regex.Pattern;

import static com.sun.org.apache.xml.internal.serializer.utils.Utils.messages;


/**
 * Created by Anas on 11/1/2016.
 */
public final class StreamingKafkaTest {

        private static final Pattern SPACE = Pattern.compile(",");

        public static void main(String[] args) throws Exception {

            //System.setProperty("hadoop.home.dir", "c:\\hadoop-2.7.3\\");
            SparkConf conf = new SparkConf().setAppName("StreamingTest").setMaster("local[*]").set("spark.executor.memory","8g").set("spark.driver.maxResultSize","2g");

            JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(3000));

            Set<String> topicsSet = new HashSet<>(Arrays.asList("plt-input"));
            Map<String, String> kafkaParams = new HashMap<>();
            kafkaParams.put("metadata.broker.list", "localhost:9092");
            //kafkaParams.put("bootstrap.servers", "localhost:2181");


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
            JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
                @Override
                public Iterator<String> call(String x) {
                    return Arrays.asList(SPACE.split(x)).iterator();
                }
            });
            JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
                    new PairFunction<String, String, Integer>() {
                        @Override
                        public Tuple2<String, Integer> call(String s) {
                            return new Tuple2<>(s, 1);
                        }
                    }).reduceByKey(
                    new Function2<Integer, Integer, Integer>() {
                        @Override
                        public Integer call(Integer i1, Integer i2) {
                            return i1 + i2;
                        }
                    });
            wordCounts.print();

            // Start the computation
            ssc.start();
            ssc.awaitTermination();
        }
}
