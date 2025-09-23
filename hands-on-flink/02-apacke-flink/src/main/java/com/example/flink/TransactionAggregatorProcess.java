package com.example.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.*;

public class TransactionAggregatorProcess extends ProcessWindowFunction<Transaction, String, String, TimeWindow> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(String key, Context ctx, Iterable<Transaction> input, Collector<String> out) throws Exception {
        String[] parts = key.split("\\|");
        String userId = parts[0];
        String category = parts[1];
        System.out.println("Triggered window for key: " + key);

        double sum = 0.0;
        long count = 0;
        for (Transaction t : input) {
            System.out.println("  Event in window: " + t);
            sum += t.amount;
            count++;
        }
        double avg = (count == 0) ? 0 : sum / count;

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("category", category);
        summary.put("windowStart", ctx.window().getStart());
        summary.put("windowEnd", ctx.window().getEnd());
        summary.put("totalAmount", sum);
        summary.put("transactionCount", count);
        summary.put("averageAmount", avg);
        summary.put("highSpender", sum > 500);

        out.collect(mapper.writeValueAsString(summary));
    }
}
