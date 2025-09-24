package com.example.flink;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

// A custom ProcessWindowFunction that uses a ValueState to maintain a running total
// and emit a single aggregated result at the end of the window.
public class TransactionAggregator extends ProcessWindowFunction<Transaction, String, String, TimeWindow> {

    // A state descriptor for our ValueState, which will hold the running total amount.
    private transient ValueState<Double> totalAmountState;

    // The open() method is called once per key and task and is where we initialize state.
    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Double> descriptor =
                new ValueStateDescriptor<>(
                        "total-amount-state", // the name of the state
                        Types.DOUBLE,         // type information for the state
                        0.0);                 // the default value if state is empty
        this.totalAmountState = getRuntimeContext().getState(descriptor);
    }

    // This method is called for each window and collects the final result.
    @Override
    public void process(String key, Context context, Iterable<Transaction> elements, Collector<String> out) throws Exception {
        // We're iterating over the elements to update the state.
        // Flink's windowing takes care of collecting all elements for the window,
        // and we're using a ProcessWindowFunction to get access to the window context.
        double currentTotal = 0.0;
        for (Transaction tx : elements) {
            currentTotal += tx.amount;
        }

        // We can now update our state with the final total for this window.
        // This state is managed and checkpointed by Flink automatically.
        totalAmountState.update(currentTotal);

        // Build the aggregated JSON output
        String[] keyParts = key.split("\\|");
        String userId = keyParts[0];
        String category = keyParts[1];

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long windowStart = context.window().getStart();
        long windowEnd = context.window().getEnd();

        String result = String.format(
                "{\"userId\":\"%s\",\"category\":\"%s\",\"window_start\":\"%s\",\"window_end\":\"%s\",\"total_amount\":%.2f}",
                userId,
                category,
                dateFormat.format(new Date(windowStart)),
                dateFormat.format(new Date(windowEnd)),
                totalAmountState.value()
        );

        out.collect(result);
    }
}