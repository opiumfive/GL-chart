package com.opiumfive.glchart.data;

import java.util.List;

public class Data {
    private List<Chart> charts;

    public Data(List<Chart> charts) {
        this.charts = charts;
    }

    public Chart getChart(int idx) {
        return charts.get(idx);
    }
}
