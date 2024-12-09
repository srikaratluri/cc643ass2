import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;

public class ModelTrainer {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("WineQualityTrainer")
                .getOrCreate();

        try {
            // Load the training dataset with the correct delimiter and handle extra quotes in column names
            Dataset<Row> trainingData = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .option("delimiter", ";")  // Set the correct delimiter
                    .csv("TrainingDataset.csv");

            // Clean column names by removing extra quotes and spaces
            for (String col : trainingData.columns()) {
                trainingData = trainingData.withColumnRenamed(col, col.replace("\"", "").trim());
            }

            // Automatically determine feature columns (exclude the target column "quality")
            String targetCol = "quality";
            String[] featureCols = trainingData.columns();
            featureCols = java.util.Arrays.stream(featureCols)
                    .filter(col -> !col.equals(targetCol))
                    .toArray(String[]::new);

            // Assemble feature columns
            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(featureCols)
                    .setOutputCol("features");

            Dataset<Row> preparedData = assembler.transform(trainingData);

            // Train logistic regression model
            LogisticRegression lr = new LogisticRegression()
                    .setLabelCol(targetCol)
                    .setFeaturesCol("features");

            LogisticRegressionModel model = lr.fit(preparedData);

            // Save the model to the home directory
            String modelPath = "file:///home/ubuntu/wine_quality_model";
            model.save(modelPath);

            System.out.println("Training completed. Model saved at: " + modelPath);

        } catch (Exception e) {
            System.err.println("An error occurred during model training: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Stop the Spark session
            spark.stop();
        }
    }
}

