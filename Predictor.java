import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;

public class Predictor {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("WineQualityPredictor")
                .master("local[*]")  // Use local mode or specify master node URL in cluster mode
                .getOrCreate();

        try {
            // Load the validation dataset with the correct delimiter and handle extra quotes in column names
            Dataset<Row> validationData = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .option("delimiter", ";")  // Set the correct delimiter
                    .csv("/data/ValidationDataset.csv");

            // Clean column names by removing extra quotes and spaces
            for (String col : validationData.columns()) {
                validationData = validationData.withColumnRenamed(col, col.replace("\"", "").trim());
            }

            // Automatically determine feature columns by excluding the target column "quality"
            String targetCol = "quality";
            String[] featureCols = validationData.columns();
            featureCols = java.util.Arrays.stream(featureCols)
                    .filter(col -> !col.equals(targetCol))
                    .toArray(String[]::new);

            // Assemble feature columns
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(featureCols)
                    .setOutputCol("features");

            Dataset<Row> preparedData = assembler.transform(validationData);

            // Load the pre-trained model
            LogisticRegressionModel model = LogisticRegressionModel.load("file:///data/wine_quality_model");

            // Make predictions
            Dataset<Row> predictions = model.transform(preparedData);

            // Evaluate F1 score
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setLabelCol(targetCol)
                    .setPredictionCol("prediction")
                    .setMetricName("f1");
            double f1Score = evaluator.evaluate(predictions);

            System.out.println("F1 Score: " + f1Score);

        } catch (Exception e) {
            System.err.println("An error occurred during prediction: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Stop the Spark session
            spark.stop();
        }
    }
}


