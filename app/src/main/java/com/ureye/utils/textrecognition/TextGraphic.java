package com.ureye.utils.textrecognition;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Element;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import com.ureye.BaseApplication;
import com.ureye.utils.common.GraphicOverlay;

public class TextGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "TextGraphic";

    private static final int TEXT_COLOR = Color.BLACK;
    private static final int MARKER_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Paint labelPaint;
    private final Text text;
    private final Boolean shouldGroupTextInBlocks;

    TextGraphic(GraphicOverlay overlay, Text text, Boolean shouldGroupTextInBlocks) {
        super(overlay);

        this.text = text;
        this.shouldGroupTextInBlocks = shouldGroupTextInBlocks;

        rectPaint = new Paint();
        rectPaint.setColor(MARKER_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);

        labelPaint = new Paint();
        labelPaint.setColor(MARKER_COLOR);
        labelPaint.setStyle(Paint.Style.FILL);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "Text is: " + text.getText());
        for (TextBlock textBlock : text.getTextBlocks()) {
            Log.d(TAG, "TextBlock text is: " + textBlock.getText());
            if (shouldGroupTextInBlocks) {
                drawText(textBlock.getText(), new RectF(textBlock.getBoundingBox()), TEXT_SIZE * textBlock.getLines().size() + 2 * STROKE_WIDTH, canvas);
            } else {
                for (Line line : textBlock.getLines()) {
                    Log.d(TAG, "Line text is: " + line.getText());
                    drawText(line.getText(), new RectF(line.getBoundingBox()), TEXT_SIZE + 2 * STROKE_WIDTH, canvas);
                    for (Element element : line.getElements()) {
                        Log.d(TAG, "Element text is: " + element.getText());
                    }
                }
            }
        }
    }

    private void drawText(String text, RectF rect, float textHeight, Canvas canvas) {
        float x0 = translateX(rect.left);
        float x1 = translateX(rect.right);
        rect.left = min(x0, x1);
        rect.right = max(x0, x1);
        rect.top = translateY(rect.top);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);
        float textWidth = textPaint.measureText(text);
        canvas.drawRect(rect.left - STROKE_WIDTH, rect.top - textHeight, rect.left + textWidth + 2 * STROKE_WIDTH, rect.top, labelPaint);
        canvas.drawText(text, rect.left, rect.top - STROKE_WIDTH, textPaint);
        BaseApplication.getInstance().addTextToSpeech(text);
    }

}
