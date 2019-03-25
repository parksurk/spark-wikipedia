
package wikipedia

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "src/main/resources/wikipedia/wikipedia.dat"
    val conf = new SparkConf().setAppName("SimpleApp").setMaster("local[2]").set("spark.executor.memory", "1g")
    val sc = new SparkContext(conf)
    val minPartitions = 2
    val logData = sc.textFile(logFile, minPartitions).cache()
    val numScalas = logData.filter(line => line.contains("Scala")).count()
    val numJavaScripts = logData.filter(line => line.contains("JavaScript")).count()
    println(s"Lines with Scala: $numScalas, Lines with JavaScript: $numJavaScripts")
    sc.stop()
  }
}
