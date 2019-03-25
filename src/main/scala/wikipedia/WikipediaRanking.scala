
package wikipedia

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

import org.apache.spark.rdd.RDD
case class WikipediaArticle(title: String, text: String)


object WikipediaRanking {

  val langs = List(
    "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
    "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")
  val cores = 5
  val conf = new SparkConf().setAppName("WikipediaRanking")
    .setMaster(s"local[$cores]").set("spark.executor.memory", "8g")
  val sc = new SparkContext(conf)

}