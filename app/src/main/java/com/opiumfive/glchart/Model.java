package com.opiumfive.glchart;

import com.opiumfive.glchart.data.Chart;
import com.opiumfive.glchart.data.Column;
import com.opiumfive.glchart.graphics.DateRibbonComponent;
import com.opiumfive.glchart.graphics.GridComponent;
import com.opiumfive.glchart.graphics.typewriter.Typewriter;
import com.opiumfive.glchart.transitions.LinearTransition;
import com.opiumfive.glchart.transitions.SinTransition;
import com.opiumfive.glchart.transitions.Transition;

import java.util.*;

public class Model {

    private final Chart chart;
    private final Typewriter tw;

    private double scrollLeft;
    private double scrollRight;

    private int width;
    private int height;

    private double maxFactor;
    private double smoothMaxFactor = -1;

    private List<Transition> animationList = new ArrayList<>();
    private List<GridComponent> gridComponents = new ArrayList<>();

    private DateRibbonComponent dateRibbonComponent;
    private int dateRibbonKFactor = 1;

    private final PopupState popupState;

    private volatile boolean dirty = false;

    public Model(Chart chart, Typewriter tw) {
        this.chart = chart;
        this.tw = tw;
        this.popupState = new PopupState(this);

        for (Column c: chart.getYColumns()) {
            c.setColor(chart.getColor(c.getLabel()));
        }
    }

    public double getScrollLeft() {
        return scrollLeft;
    }

    public double getScrollRight() {
        return scrollRight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setScrollLeft(double scrollLeft) {
        this.scrollLeft = scrollLeft;

        refresh();
    }

    public void setScrollRight(double scrollRight) {
        this.scrollRight = scrollRight;

        refresh();
    }

    public void setScroll(double scrollLeft, double scrollRight) {
        if (scrollLeft >= 0 && scrollRight <= 1 && scrollLeft < scrollRight) {
            this.scrollLeft = scrollLeft;
            this.scrollRight = scrollRight;

            refresh();
        }
    }

    public void refresh() {
        dirty = true;

        popupState.refresh();
        refreshMaxFactor();
    }

    private void refreshMaxFactor() {
        double max = 0;
        for (Column column: chart.getYColumns()) {
            if (column.isVisible()) {
                max = Math.max(max, column.getMaxValue(scrollLeft, scrollRight));
            }
        }

        max = Math.ceil(max / 50) * 50;

        popupState.refresh();
        while (popupState.isVisible() && getY(max, popupState.maxMarkedValue()) < popupState.getDimensions().bottom) {
            max *= 1.2;
            popupState.refresh();
        }

        if (max != maxFactor) {
            if (smoothMaxFactor == -1) {
                smoothMaxFactor = maxFactor;
            }

            animationList.add(new SinTransition((float) maxFactor, (float) max, 20) {
                @Override
                public boolean tick() {
                    boolean b = super.tick();
                    smoothMaxFactor += getDelta();
                    return b;
                }
            });

            GridComponent fadeInComponent = gridComponents.get(0);
            fadeInComponent.setMaxFactor(max);

            GridComponent fadeOutComponent = gridComponents.get(1);
            fadeOutComponent.setMaxFactor(maxFactor);

            fadeInComponent.show();
            fadeOutComponent.hide();

            maxFactor = max;
        }

        refreshK();
    }

    private void refreshK() {
        if (width == 0) {
            return;
        }

        Column xColumn = chart.getXColumn();
        double xDelta = xColumn.getMaxValue() -  xColumn.getMinValue();
        xDelta *= (getScrollRight() - getScrollLeft());

        double xFactor = width / xDelta;

        int oldValue = dateRibbonKFactor;
        dateRibbonKFactor = (int) Math.max(0, Math.log(ViewConstants.WIDTH_LIMIT / (xColumn.getDivision() * xFactor)) / Math.log(2));

        if (dateRibbonComponent != null && dateRibbonKFactor != oldValue) {
            dateRibbonComponent.onFactorUpdated(oldValue, dateRibbonKFactor);
        }
    }

    public Chart getChart() {
        return chart;
    }

    public void setVisible(String label, final boolean visible) {
        final Column yColumn = chart.getYColumn(label);
        if (yColumn.isVisible() != visible) {
            yColumn.setVisible(visible);

            if (visible) {
                animationList.add(new LinearTransition(0f, 1.0f, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incOpacity(getDelta());
                        return tick;
                    }
                });
            } else {
                animationList.add(new LinearTransition(1.0f, 0f, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incOpacity(getDelta());
                        return tick;
                    }
                });
            }
        }

        refreshScrollScaleFactors();
        refresh();
    }

    private void refreshScrollScaleFactors() {
        double max = 0;
        for (Column yColumn: chart.getYColumns()) {
            if (yColumn.isVisible()) {
                max = Math.max(max, yColumn.getMaxValue());
            }
        }

        for (final Column yColumn: chart.getYColumns()) {
            if (yColumn.isVisible()) {
                float prevScaleFactor = yColumn.getScrollYScaleFactor();
                float newScaleFactor = (float) (yColumn.getMaxValue() / max);

                yColumn.setScrollYScaleFactor(newScaleFactor);

                animationList.add(new LinearTransition(prevScaleFactor, newScaleFactor, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incAnimatedScrollScaleFactor(getDelta());
                        return tick;
                    }
                });
            }
        }
    }

    public boolean tick() {
        Set<Transition> removed = new HashSet<>();
        for (Transition v : animationList) {
            if (!v.tick()) {
                removed.add(v);
            }
        }

        animationList.removeAll(removed);
        return !animationList.isEmpty();
    }

    public double getSmoothMaxFactor() {
        return smoothMaxFactor;
    }

    public Collection<GridComponent> getGridComponents() {
        return gridComponents;
    }

    public void setRibbonComponent(DateRibbonComponent dateRibbonComponent) {
        this.dateRibbonComponent = dateRibbonComponent;
    }

    public void setTooltipPosition(int index, double date) {
        popupState.setPosition(index, date);
    }

    public double getY(double maxFactor, double value) {
        int chartHeight = height - ViewConstants.CHART_OFFSET;
        return chartHeight - (value / maxFactor * chartHeight);
    }

    public double getY(double value) {
        return getY(smoothMaxFactor, value);
    }

    public double getX(double date) {
        Column xColumn = chart.getXColumn();

        double xDelta = xColumn.getMaxValue() - xColumn.getMinValue();
        double min = xColumn.getMinValue() + xDelta * scrollLeft;
        xDelta *= (scrollRight - scrollLeft);

        return width *  (date - min) / xDelta;
    }

    public PopupState getPopupState() {
        return popupState;
    }

    public Typewriter getTypewriter() {
        return tw;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void updateDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
