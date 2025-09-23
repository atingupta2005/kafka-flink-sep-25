package com.example.flink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvTransformExample {

    public static void main(String[] args) throws Exception {
        String defaultInputPath = "/data/input.csv";
        String defaultOutputPath = "/data/output.csv";

        // Use input parameters if provided; else fallback to default paths
        String inputFilePath = (args.length >= 1 && args[0] != null && !args[0].isEmpty())
                ? args[0] : defaultInputPath;

        String outputFilePath = (args.length >= 2 && args[1] != null && !args[1].isEmpty())
                ? args[1] : defaultOutputPath;

        // Delete output CSV if it exists
        deleteOutputFileIfExists(outputFilePath);

        // Set up Flink batch execution environment
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // Read CSV with 3 fields: int id, String name, double salary
        DataSet<Tuple3<Integer, String, Double>> input = env.readCsvFile(inputFilePath)
                .ignoreFirstLine()
                .parseQuotedStrings('"')
                .ignoreInvalidLines()
                .types(Integer.class, String.class, Double.class);

        // Transformation: increase salary by 10%
        DataSet<Tuple3<Integer, String, Double>> transformed = input.map(
                new MapFunction<Tuple3<Integer, String, Double>, Tuple3<Integer, String, Double>>() {
                    @Override
                    public Tuple3<Integer, String, Double> map(Tuple3<Integer, String, Double> value) {
                        Double updatedSalary = value.f2 * 1.10;
                        return new Tuple3<>(value.f0, value.f1, updatedSalary);
                    }
                });

        // Write transformed data to output CSV file, overwrite if exists
        transformed.writeAsCsv(outputFilePath, "\n", ",").setParallelism(1);

        // Execute the Flink batch job
        env.execute("CSV Transformation Example with Default Paths");
    }

    private static void deleteOutputFileIfExists(String outputFilePath) {
        File outputFile = new File(outputFilePath);
        if (outputFile.exists() && outputFile.isFile()) {
            if (outputFile.delete()) {
                System.out.println("Deleted existing output file: " + outputFilePath);
            } else {
                System.err.println("Failed to delete existing output file: " + outputFilePath);
            }
        }
    }
}
