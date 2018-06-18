//  Developing application in eclipse with Spark, Scala and Maven

// POM.xml dependencies
// SparkCore : https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.11/1.6.3
// SparkSql: https://mvnrepository.com/artifact/org.apache.spark/spark-sql_2.11/1.6.3
// Project: https://github.com/AbhishekSolanki/Spark/tree/master/spark/code/SparkExample

// src/main/scala/SparkCoreExample.scala
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object SparkCoreExample {
   def main(args: Array[String]): Unit = {
     // create spark context by creating new SparkContext object and pass SparkConfig object in constructor
    val sc = new SparkContext(
        new SparkConf().setAppName("SparkCoreExample").setMaster("local[2]")
        )
    // create RDD by specifying the complete HDFS path
    val fileRDD = sc.textFile("hdfs://localhost:8020/user/cloudera/dataset/retail_db/orders/part-00000")
    
    // Perform filter, transformation and actions
    val julyCompleteOrderWithSum = fileRDD.filter( x=> {
	    val splitedData = x.split(",")
	    if(splitedData(1).contains("2013-07") && splitedData(3)=="COMPLETE") true else false
     }).map(x => x.split(",")(2).toFloat).sum

// Display result     
println("Revenue of 'COMPLETED' orders for July: "+julyCompleteOrderWithSum)
  }
}


// sec/main/scala/SparkSqlExample.scala
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.spark.SparkConf

// Defining the named columns structure and its datatypes using case classes
//defined class Orders
case class Orders(order_id: Int, order_date: String, order_customer_id: Int, order_status: String)

object SparkSqlExample {
    def main(args: Array[String]): Unit = {
            // create spark context by creating new SparkContext object and pass SparkConfig object in constructor
            val sc = new SparkContext(
                    new SparkConf().setAppName("SparkCoreExample").setMaster("local[2]")
                    )
                    // create sqlContext by using sparkContext
                    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
                    import sqlContext.implicits._

                  // Create object of class for each record and convert to dataframe 
                    val ordersData = sc.textFile("hdfs://localhost:8020/user/cloudera/dataset/retail_db/orders/part-00000").map( x => {
                        val rec = x.split(",")
                                Orders(rec(0).toInt, rec(1), rec(2).toInt, rec(3))
                    }).toDF()
                    //ordersData: org.apache.spark.sql.DataFrame = [order_id: int, order_date: string, order_customer_id: int, order_status: string]

                    // data needs to be mapped to a table name with registerTempTable function
                    ordersData.registerTempTable("ordersTable")

                    // using the table name mentioned in the registerTempTable perform queries
                    val ordersCount =sqlContext.sql("select count(*) from ordersTable")
                    //ordersCount: org.apache.spark.sql.DataFrame = [_c0: bigint]

                    ordersCount.show

    }
}



// Export -> jar -> SparkExample.jar
// Running via spark-submit
// spark-submit --class SparkCoreExample SparkExample.jar
//spark-submit --class SparkSqlExample SparkExample.jar

