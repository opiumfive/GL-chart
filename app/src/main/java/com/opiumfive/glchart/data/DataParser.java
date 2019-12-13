package com.opiumfive.glchart.data;

import android.util.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataParser {
    private final JsonReader reader;

    public DataParser(JsonReader reader) {
        this.reader = reader;
    }

    public Data parse() throws IOException {
        return new Data(parseChartArray());
    }

    private List<Chart> parseChartArray() throws IOException {
        List<Chart> charts = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            charts.add(parseChart());
        }
        reader.endArray();
        return charts;
    }

    private Chart parseChart() throws IOException {
        reader.beginObject();
        Chart chart = new Chart();

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "columns":
                    readColumns(chart);
                    break;
                case "types":
                    chart.setTypes(readMap(chart));
                    break;
                case "names":
                    chart.setNames(readMap(chart));
                    break;
                case "colors":
                    chart.setColors(readMap(chart));
                    break;
                default:
                    reader.skipValue();
            }
        }

        reader.endObject();
        return chart;
    }

    private void readColumns(Chart chart) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            chart.addColumn(readColumn(chart));
        }

        reader.endArray();
    }

    private Map<String, String> readMap(Chart chart) throws IOException {
        Map<String, String> types = new LinkedHashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            String value = reader.nextString();
            types.put(name, value);
        }
        reader.endObject();
        return types;
    }

    private Column readColumn(Chart chart) throws IOException {
        reader.beginArray();

        String label = reader.nextString();

        List<Double> data = new ArrayList<>();
        while (reader.hasNext()) {
            data.add(reader.nextDouble());
        }
        reader.endArray();
        return new Column(label, data);
    }
}
