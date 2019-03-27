
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

  val wikiRdd: RDD[WikipediaArticle] = sc.textFile(WikipediaData.filePath).map {
    line => WikipediaData.parse(line)
  }.cache()

  /*
   * Returns the number of articles on which the language `lang` occurs.
   *  Hint1: consider using method `aggregate` on RDD[T].
   *  Hint2: should you count the "Java" language when you see "JavaScript"?
   *  Hint3: the only whitespaces are blanks " "
   *  Hint4: no need to search in the title :)
   */
  def occurrencesOfLang(lang: String, rdd: RDD[WikipediaArticle]): Int = {
    val count: (Int, WikipediaArticle) => Int = (count, article) => {
      if (article.text.split(" ").contains(lang)) {
        count + 1
      } else {
        count
      }
    }
    val add: (Int, Int) => Int = _ + _
    val default = 0
    rdd.aggregate(default)(count, add)
  }

  /* (1) Use `occurrencesOfLang` to compute the ranking of the languages
   *     (`val langs`) by determining the number of Wikipedia articles that
   *     mention each language at least once. Don't forget to sort the
   *     languages by their occurrence, in decreasing order!
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangs(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] = {
    langs.map {
      lang => (lang, occurrencesOfLang(lang, rdd))
    }.sortBy(pair => pair._2)(Ordering[Int].reverse)
  }

  /* Compute an inverted index of the set of articles, mapping each language
 * to the Wikipedia pages in which it occurs.
 */
  def makeIndex(langs: List[String], rdd: RDD[WikipediaArticle]): RDD[(String, Iterable[WikipediaArticle])] = {
    rdd.flatMap {
      article =>
        langs.filter {
          lang => article.text.split(" ").contains(lang)
        }.map {
          lang => (lang, article)
        }
    }.groupByKey()
  }


  /* (2) Compute the language ranking again, but now using the inverted index. Can you notice
   *     a performance improvement?
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangsUsingIndex(index: RDD[(String, Iterable[WikipediaArticle])]): List[(String, Int)] = {
    val asc = false
    index.mapValues {
      articles => articles.size
    }.sortBy(pair => pair._2, asc).collect().toList
  }

  /* (3) Use `reduceByKey` so that the computation of the index and the ranking are combined.
   *     Can you notice an improvement in performance compared to measuring *both* the computation of the index
   *     and the computation of the ranking? If so, can you think of a reason?
   *
   *   Note: this operation is long-running. It can potentially run for
   *   several seconds.
   */
  def rankLangsReduceByKey(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] = {
    val asc = false
    rdd.flatMap {
      article =>
        langs.filter {
          lang => article.text.split(" ").contains(lang)
        }.map {
          lang => (lang, 1)
        }
    }.reduceByKey(_ + _).sortBy(pair => pair._2, asc).collect().toList
  }

  def main(args: Array[String]) {

    /* Languages ranked according to (1) */
    val langsRanked: List[(String, Int)] = timed("Part 1: naive ranking", rankLangs(langs, wikiRdd))

    /* An inverted index mapping languages to wikipedia pages on which they appear */
    def index: RDD[(String, Iterable[WikipediaArticle])] = makeIndex(langs, wikiRdd)

    /* Languages ranked according to (2), using the inverted index */
    val langsRanked2: List[(String, Int)] = timed("Part 2: ranking using inverted index", rankLangsUsingIndex(index))

    /* Languages ranked according to (3) */
    val langsRanked3: List[(String, Int)] = timed("Part 3: ranking using reduceByKey", rankLangsReduceByKey(langs, wikiRdd))

    /* Output the speed of each ranking */
    println(timing)
    sc.stop()
  }

  val timing = new StringBuffer
  def timed[T](label: String, code: => T): T = {
    val start = System.currentTimeMillis()
    val result = code
    val stop = System.currentTimeMillis()
    timing.append(s"$result\nProcessing $label took ${stop - start} ms.\n")
    result
  }

}