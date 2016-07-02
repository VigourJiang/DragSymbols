package com.lightcone.dragsymbols;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

/*
Demonstration of one way to put a set of draggable symbols on screen.
Adapted loosely from material discussed in
http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
See also
http://android-developers.blogspot.com/2010/07/how-to-have-your-cupcake-and-eat-it-too.html
*/

public class DragginSlayer extends View {

    // Colors for background and text
    private static final int BACKGROUND_COLOR = Color.argb(255, 210, 210, 210);
    private static final int HEADER_COLOR = Color.argb(255, 190, 190, 190);
    private static final int TEXT_COLOR = Color.argb(255, 0, 0, 0);

    private int numberSymbols; // Total number of symbols to use
    private Drawable[] symbol; // Array of symbols (dimension numberSymbols)
    private float[] X; // Current x coordinate, upper left corner of symbol
    private float[] Y; // Current y coordinate, upper left corner of symbol
    private int[] symbolWidth; // Width of symbol
    private int[] symbolHeight; // Height of symbol
    private float[] lastTouchX; // x coordinate of symbol at last touch
    private float[] lastTouchY; // y coordinate of symbol at last touch
    private int symbolSelected; // Index of symbol last touched (-1 if none)
    private Paint paint;

    // Following define upper left and lower right corners of display stage rectangle
    private int stageX1 = 0;
    private int stageY1 = MainActivity.topMargin;
    private int stageX2 = MainActivity.screenWidth;
    private int stageY2 = MainActivity.screenHeight;

    private boolean isDragging = false; // True if some symbol is being dragged

    // Simplest default constructor. Not used, but prevents a warning message.
    public DragginSlayer(Context context) {
        super(context);
    }

    public DragginSlayer(Context context, float[] X, float[] Y,
                         int[] symbolIndex) {

        // Call through to simplest constructor of View superclass
        super(context);

        // Set up local arrays defining symbol positions with the initial
        // positions passed as arguments in the constructor

        this.X = X;
        this.Y = Y;

        numberSymbols = X.length;
        symbol = new Drawable[numberSymbols];
        symbolWidth = new int[numberSymbols];
        symbolHeight = new int[numberSymbols];
        lastTouchX = new float[numberSymbols];
        lastTouchY = new float[numberSymbols];

        // Fill the symbol arrays with data
        for (int i = 0; i < numberSymbols; i++) {

            // Handle method getDrawable deprecated as of API 22
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Theme required but set to null since no styling for it
                symbol[i] = context.getResources().getDrawable(symbolIndex[i],null);
            } else {
                symbol[i] = context.getResources().getDrawable(symbolIndex[i]);
            }

            symbolWidth[i] = symbol[i].getIntrinsicWidth();
            symbolHeight[i] = symbol[i].getIntrinsicHeight();
            symbol[i].setBounds(0, 0, symbolWidth[i], symbolHeight[i]);
        }

        // Set up the Paint object that will control format of screen draws
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(36);
        paint.setStrokeWidth(0);
    }

      /*
         * Process MotionEvents corresponding to screen touches and drags.
         * MotionEvent reports movement (mouse, pen, finger, trackball) events. The
         * MotionEvent method getAction() returns the kind of action being performed
         * as an integer constant of the MotionEvent class, with possible values
         * ACTION_DOWN, ACTION_MOVE, ACTION_UP, and ACTION_CANCEL. Thus we can
         * switch on the returned integer to determine the kind of event and the
         * appropriate action.
         */

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();

        switch (action) {

            // MotionEvent class constant signifying a finger-down event

            case MotionEvent.ACTION_DOWN: {

                isDragging = false;

                // Get coordinates of touch event
                final float x = ev.getX();
                final float y = ev.getY();

                // Initialize. Will be -1 if not within the current bounds of some
                // symbol.

                symbolSelected = -1;

                // Determine if touch within bounds of one of the symbols

                for (int i = 0; i < numberSymbols; i++) {
                    if ((x > X[i] && x < (X[i] + symbolWidth[i]))
                            && (y > Y[i] && y < (Y[i] + symbolHeight[i]))) {
                        symbolSelected = i;
                        break;
                    }
                }

                // If touch within bounds of a symbol, remember start position for
                // this symbol

                if (symbolSelected > -1) {
                    lastTouchX[symbolSelected] = x;
                    lastTouchY[symbolSelected] = y;
                }
                break;
            }

            // MotionEvent class constant signifying a finger-drag event

            case MotionEvent.ACTION_MOVE: {

                // Only process if touch selected a symbol
                if (symbolSelected > -1) {
                    isDragging = true;
                    final float x = ev.getX();
                    final float y = ev.getY();

                    // Calculate the distance moved
                    final float dx = x - lastTouchX[symbolSelected];
                    final float dy = y - lastTouchY[symbolSelected];

                    // Move the object selected. Note that we are simply
                    // illustrating how to drag symbols. In an actual application,
                    // you would probably want to add some logic to confine the symbols
                    // to a region the size of the visible stage or smaller.

                    X[symbolSelected] += dx;
                    Y[symbolSelected] += dy;

                    // Remember this touch position for the next move event of this object
                    lastTouchX[symbolSelected] = x;
                    lastTouchY[symbolSelected] = y;

                    // Request a redraw
                    invalidate();

                }
                break;
            }

            // MotionEvent class constant signifying a finger-up event

            case MotionEvent.ACTION_UP:
                isDragging = false;
                invalidate(); // Request redraw
                break;

        }
        return true;
    }

    // This method will be called each time the screen is redrawn. The draw is
    // on the Canvas object, with formatting controlled by the Paint object.
    // When to redraw is under Android control, but we can request a redraw
    // using the method invalidate() inherited from the View superclass.

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw backgrounds
        drawBackground(paint, canvas);

        // Draw all draggable symbols at their current locations
        for (int i = 0; i < numberSymbols; i++) {
            canvas.save();
            canvas.translate(X[i], Y[i]);
            symbol[i].draw(canvas);
            canvas.restore();
        }
        isDragging = false;
    }

    // Method to draw the background for the screen. Invoked from onDraw each
    // time the screen is redrawn.

    private void drawBackground(Paint paint, Canvas canvas) {

        // Draw header bar background
        paint.setColor(HEADER_COLOR);
        canvas.drawRect(0, 0, stageX2, stageY2, paint);

        // Draw main stage background
        paint.setColor(BACKGROUND_COLOR);
        canvas.drawRect(stageX1, stageY1, stageX2, stageY2, paint);

        // If dragging a symbol, display its x and y coordinates in a readout
        if (isDragging) {
            paint.setColor(TEXT_COLOR);
            canvas.drawText("X = " + X[symbolSelected],
                    MainActivity.screenWidth / 2,
                    MainActivity.topMargin / 2 - 10, paint);
            canvas.drawText("Y = " + Y[symbolSelected],
                    MainActivity.screenWidth / 2,
                    MainActivity.topMargin / 2 + 25, paint);
        }
    }
}
