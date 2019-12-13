package com.opiumfive.glchart;

import android.view.MotionEvent;
import com.opiumfive.glchart.data.Column;

public class TouchListener {
    private final Model model;

    private DragState state = DragState.NONE;
    private float evX;
    private float evY;

    public TouchListener(Model model) {
        this.model = model;
    }

    public void onTouchEvent(MotionEvent e) {
        synchronized (Model.class) {
            touchScroll(e);
            touchChart(e);
        }
    }

    private void touchChart(MotionEvent e) {
        if (e.getY() < model.getHeight() - ViewConstants.CHART_OFFSET) {
            float mouseX = e.getX();

            int width = model.getWidth();
            double scrollLeft = model.getScrollLeft();
            double scrollRight = model.getScrollRight();

            double offsetX = scrollLeft + (scrollRight - scrollLeft) * mouseX / width;
            Column xColumn = model.getChart().getXColumn();
            int dateIndex = xColumn.nearestIndex(offsetX);
            double date = xColumn.getValue(dateIndex);

            model.setTooltipPosition(dateIndex, date);
            model.refresh();
        }
    }

    private void touchScroll(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int width = model.getWidth();

            double xleft = model.getScrollLeft() * width;
            double xright = model.getScrollRight() * width;

            int ypos = model.getHeight() - ViewConstants.SCROLL_HEIGHT;
            if (!(e.getY() < ypos) && !(e.getY() > model.getHeight())) {
                if (Math.abs(e.getX() - xleft) < 30) {
                    state = DragState.LEFT;
                } else
                if (Math.abs(e.getX() - xright) < 30) {
                    state = DragState.RIGHT;
                } else
                if (e.getX() > xleft && e.getX() < xright) {
                    state = DragState.BOTH;
                }
            }
        } else
        if (e.getAction() == MotionEvent.ACTION_UP) {
            state = DragState.NONE;
        } else
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            int width = model.getWidth();
            float dx = e.getX() - evX;

            switch (state) {
                case LEFT:
                    model.setScrollLeft(model.getScrollLeft() + (double) dx / width);
                    break;
                case RIGHT:
                    model.setScrollRight(model.getScrollRight() + (double) dx / width);
                    break;
                case BOTH:
                    model.setScroll(model.getScrollLeft() + (double) dx / width,
                            model.getScrollRight() + (double) dx / width);
                    break;
                case NONE:
                    break;
            }
        }

        evX = e.getX();
        evY = e.getY();
    }
}
