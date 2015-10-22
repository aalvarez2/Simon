package com.example.jeff.simon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ButtonGridView extends View implements Simon.Listener {

    // measurements

    private static final int BUTTON_GRID_SIZE = 2;

    private static final float BUTTON_PADDING = 0.01f;

    private float buttonCellSize;

    private float scale;

    // drawing tools

    private Bitmap buttonIdleBitmap;
    private Bitmap buttonPressedBitmap;
    private Bitmap redOnBitmap;
    private Bitmap redOffBitmap;
    private Bitmap blueOnBitmap;
    private Bitmap blueOffBitmap;
    private Bitmap greenOnBitmap;
    private Bitmap greenOffBitmap;
    private Bitmap yellowOnBitmap;
    private Bitmap yellowOffBitmap;

    // model

    private Simon model;

    public ButtonGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ButtonGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonGridView(Context context) {
        super(context);
        init();
    }

    private void init() {
        initDrawingInstruments();
    }

    private void initDrawingInstruments() {
        Resources resources = getContext().getResources();

        redOnBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_red_on);
        redOffBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_red_off);
        blueOnBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_blue_on);
        blueOffBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_blue_off);
        greenOnBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_green_on);
        greenOffBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_green_off);
        yellowOnBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_yellow_on);
        yellowOffBitmap = BitmapFactory.decodeResource(resources, R.drawable.ex_yellow_off);
    }

    public void setSimonCloneModel(Simon model) {
        if (model != null) {
            model.removeListener(this);
        }
        this.model = model;
        if (model != null) {
            model.addListener(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        scale = canvas.getClipBounds().width();

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(scale, scale);

        drawButtons(canvas);

        canvas.restore();
    }

    private void drawButtons(Canvas canvas) {
        for (int row = 0; row < BUTTON_GRID_SIZE; ++row) {
            for (int col = 0; col < BUTTON_GRID_SIZE; ++col) {
                drawButton(canvas, row, col);
            }
        }
    }

    private void drawButton(Canvas canvas, int row, int col) {
        buttonCellSize = 1.0f / BUTTON_GRID_SIZE;

        float buttonCellTop = row * buttonCellSize;
        float buttonCellLeft = col * buttonCellSize;

        float buttonTop = buttonCellTop + BUTTON_PADDING;
        float buttonLeft = buttonCellLeft + BUTTON_PADDING;

        float buttonSize = (buttonCellSize - BUTTON_PADDING * 2);

        Bitmap bitmap = getBitmapForButton(row, col);
        float pixelSize = canvas.getClipBounds().width();
        float bitmapScaleX = (pixelSize / bitmap.getWidth()) * buttonSize;
        float bitmapScaleY = (pixelSize / bitmap.getHeight()) * buttonSize;

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(bitmapScaleX, bitmapScaleY);
        canvas.drawBitmap(bitmap, buttonLeft / bitmapScaleX, buttonTop / bitmapScaleY, null);
        canvas.restore();
    }

    private Bitmap getBitmapForButton(int row, int col) {
        boolean pressed = model.isButtonPressed(getButtonIndex(row, col));
        int butnum = getButtonIndex(row, col);
        switch (butnum) {
            case 0:
                return pressed ?
                        greenOnBitmap : greenOffBitmap;
            case 1:
                return pressed ?
                        redOnBitmap : redOffBitmap;
            case 2:
                return pressed ?
                        yellowOnBitmap : yellowOffBitmap;
            case 3:
                return pressed ?
                        blueOnBitmap : blueOffBitmap;
            default:
                return pressed ?
                        buttonPressedBitmap : buttonIdleBitmap;
        }
    }

    private int getButtonIndex(int row, int col) {
        return row * BUTTON_GRID_SIZE + col;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        int chosenDimension = Math.min(chosenWidth, chosenHeight);

        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return getPreferredSize();
        }
    }

    private int getPreferredSize() {
        return 300;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int buttonIndex = -1;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                buttonIndex = getButtonByCoords(event.getX(), event.getY());
                if (buttonIndex != -1) {
                    model.pressButton(buttonIndex);
                }
                return true;
            case MotionEvent.ACTION_UP:
                buttonIndex = getButtonByCoords(event.getX(), event.getY());
                if (buttonIndex != -1) {
                    model.releaseButton(buttonIndex);
                }
                model.releaseAllButtons();
                return true;
            case MotionEvent.ACTION_POINTER_2_DOWN:
                buttonIndex = getButtonByCoords(event.getX(1), event.getY(1));
                if (buttonIndex != -1) {
                    model.pressButton(buttonIndex);
                }
                return true;
            case MotionEvent.ACTION_POINTER_2_UP:
                buttonIndex = getButtonByCoords(event.getX(1), event.getY(1));
                if (buttonIndex != -1) {
                    model.releaseButton(buttonIndex);
                }
                return true;
        }
        return false;
    }

    private int getButtonByCoords(float x, float y) {
        float scaledX = x / scale;
        float scaledY = y / scale;

        double buttonCellX = Math.floor(scaledX / buttonCellSize);
        double buttonCellY = Math.floor(scaledY / buttonCellSize);

        return getButtonIndex((int) buttonCellY, (int) buttonCellX);
    }

    @Override
    public void buttonStateChanged(int index) {
        invalidate();
    }

    @Override
    public void multipleButtonStateChanged() {
        invalidate();
    }
}