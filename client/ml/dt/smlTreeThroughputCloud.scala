import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation.MulticlassMetrics

val hr = sc.textFile("/home/glenn/git/clojure-news-feed/client/ml/etl/throughput.csv")
val header = hr.first()
val records = hr.filter(r => !r.equals(header)).map(line => line.split(","))
val data = records.map { r =>
    val label = if (r(5).equals("GKE")) 0.0 else 1.0
    val features = r.slice(6, 9).map(d => d.toDouble)
    LabeledPoint(label, Vectors.dense(features))
}
val splits = data.randomSplit(Array(0.6, 0.4))
val (trainingData, testData) = (splits(0), splits(1))
val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int](0 -> 7)
val impurity = "gini"
val maxDepth = 6
val maxBins = 32
val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
val predictions = testData.map { case LabeledPoint(label, features) =>
    val prediction = model.predict(features)
    (prediction, label)
}
val metrics = new MulticlassMetrics(predictions)
val accuracy = metrics.accuracy
println(s"accuracy: $accuracy")
println("model")
println(model.toDebugString)
