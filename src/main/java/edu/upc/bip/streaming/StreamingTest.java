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
import org.apache.spark.api.java.function.Function;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;

import java.util.regex.Pattern;


/**
 * Created by Anas on 11/1/2016.
 */
public final class StreamingTest {

        private static final Pattern SPACE = Pattern.compile(",");

        public static void main(String[] args) throws Exception {

            //System.setProperty("hadoop.home.dir", "c:\\hadoop-2.7.3\\");
            SparkConf conf = new SparkConf().setAppName("StreamingTest").setMaster("local[8]");//"spark://192.168.56.1:7077");

            JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(3000));
		
            JavaReceiverInputDStream<SparkFlumeEvent> flumeStream =
                    FlumeUtils.createPollingStream(ssc, "127.0.0.1", 11111);
            flumeStream.count().map((Function<Long, String>) in -> "Received " + in + " flume events.").print();

            ssc.start();
            ssc.awaitTermination();

        }
}
