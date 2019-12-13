package com.opiumfive.glchart.graphics;

import com.opiumfive.glchart.Model;
import com.opiumfive.glchart.data.Chart;
import com.opiumfive.glchart.data.Column;

import java.util.ArrayList;
import java.util.List;

public class ScrollChartComponent {
    private final List<ChartComponent> chartComponents = new ArrayList<>();
    private List<ScrollChartColumn> subcomponents = new ArrayList<>();

    public ScrollChartComponent(Model model, SpriteRenderer spriteRenderer) {
        Chart chart = model.getChart();

        Column xColumn = chart.getXColumn();
        for (Column yColumn: chart.getYColumns()) {
            int color = chart.getColor(yColumn.getLabel());
            subcomponents.add(new ScrollChartColumn(xColumn, yColumn, color));
            chartComponents.add(new ChartComponent(model, xColumn, yColumn, color));
        }
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        for (ScrollChartColumn comp: subcomponents) {
            comp.draw(width, height, mMVPMatrix);
        }

        for (ChartComponent cmp: chartComponents) {
            cmp.draw(width, height, mMVPMatrix);
        }
    }
}
