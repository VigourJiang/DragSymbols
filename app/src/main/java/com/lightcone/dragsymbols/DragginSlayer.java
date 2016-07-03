package com.lightcone.dragsymbols;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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

    private static final int maxInstances = 12; // Max symbol instances permitted onstage

    private int numberSymbols; // Total number of symbols to use
    private int numberInstances;  // Total number of symbol instances onstage
    private Drawable[] symbol; // Array of symbols (dimension numberSymbols)
    private int[] symbolIndex;  // Index of Drawable resource (R.drawable.symbol)
    private float[] X; // Current x coordinate, upper left corner of symbol
    private float[] Y; // Current y coordinate, upper left corner of symbol
    private Drawable[] symbol0;    // Array of symbols (dimension numberSymbols)
    private float[] X0;    // Initial x coordinate, upper left corner of symbol i
    private float[] Y0;    // Initial y coordinate, upper left corner of symbol i
    private int[] symbolWidth; // Width of symbol
    private int[] symbolHeight; // Height of symbol
    private float[] lastTouchX; // x coordinate of symbol at last touch
    private float[] lastTouchY; // y coordinate of symbol at last touch
    private int symbolSelected; // Index of symbol last touched (-1 if none)
    private int instanceSelected;  // Index of symbol instance last touched (-1 if none)
    private Paint paint;

    // Following define upper left and lower right corners of display stage rectangle
    private int stageX1 = 0;
    private int stageY1 = MainActivity.topMargin;
    private int stageX2 = MainActivity.screenWidth;
    private int stageY2 = MainActivity.screenHeight;

    private boolean isDragging = false; // True if some symbol is being dragged

    private Context context;

    // Simplest default constructor. Not used, but prevents a warning message.
    public DragginSlayer(Context context) {
        super(context);
    }

    public DragginSlayer(Context context, float[] XX, float[] YY,
                         int[] symbolIndex) {

        // Call through to simplest constructor of View superclass
        super(context);

        this.context = context;

        // Initialize instance counter
        numberInstances = 0;

        // Set up local arrays defining symbol positions with the initial
        // positions passed as arguments in the constructor

        this.X0 = XX;
        this.Y0 = YY;
        this.symbolIndex = symbolIndex;

        numberSymbols = X0.length;
        this.X = new float[maxInstances];
        this.Y = new float[maxInstances];
        symbol0 = new Drawable[numberSymbols];
        symbol = new Drawable[maxInstances];
        symbolWidth = new int[numberSymbols];
        symbolHeight = new int[numberSymbols];
        lastTouchX = new float[maxInstances];
        lastTouchY = new float[maxInstances];

        // Fill the symbol arrays with data
        for (int i = 0; i < numberSymbols; i++) {

            // Handle method getDrawable deprecated as of API 22
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Theme required but set to null since no styling for it
                symbol0[i] = context.getResources().getDrawable(symbolIndex[i], null);
            } else {
                symbol0[i] = context.getResources().getDrawable(symbolIndex[i]);
            }

            symbolWidth[i] = symbol0[i].getIntrinsicWidth();
            symbolHeight[i] = symbol0[i].getIntrinsicHeight();
            symbol0[i].setBounds(0, 0, symbolWidth[i], symbolHeight[i]);
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

    // See android.view.View#onTouchEvent(android.view.MotionEvent)

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

                symbolSelected = -1;   // -1 if not within current bounds of symbol source
                instanceSelected = -1; // -1 if touch not within bounds of symbol instance

                // Determine if touch within bounds of one of the symbol sources offstage

                for (int i = 0; i < numberSymbols; i++) {
                    if ((x > X0[i] && x < (X0[i] + symbolWidth[i]))
                            && (y > Y0[i] && y < (Y0[i] + symbolHeight[i]))) {
                        symbolSelected = i;

                        // Warn if max number of instances has been reached (won't create any more)
                        if (numberInstances == maxInstances) {
                            String toaster = "Maximum number of instances ";
                            toaster += "(" + maxInstances + ") has been reached.";
                            Toast.makeText(context, toaster, Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                }

                // Determine if touch within bounds of one of the symbol instances onstage

                for (int i = 0; i < numberInstances; i++) {
                    int width = symbol[i].getIntrinsicWidth();
                    int height = symbol[i].getIntrinsicHeight();
                    if ((x > X[i] && x < (X[i] + width)) &&
                            (y > Y[i] && y < (Y[i] + height))) {
                        instanceSelected = i;  // Index of instance touched
                        break;
                    }
                }

                // If touch within bounds of symbol source or instance, remember start position
                // for this symbol

                if (symbolSelected > -1 || instanceSelected > -1) {
                    if (instanceSelected > -1) lastTouchX[instanceSelected] = x;
                    if (instanceSelected > -1) lastTouchY[instanceSelected] = y;
                }
                break;
            }

            // MotionEvent class constant signifying a finger-drag event

            case MotionEvent.ACTION_MOVE: {

                // Only process if touch selected a symbol and not background

                /* If touch and drag were on symbol source, and this hasn't yet been processed,
                * first create a new symbol instance (but only if the max number of instances
                * will not be exceeded).  Do it here rather than in ACTION_DOWN so that just
                * pressing the source symbol without a drag will not create a new instance.
                * */

                if (symbolSelected > -1 && instanceSelected == -1 && numberInstances < maxInstances) {

                    // Handle method getDrawable deprecated as of API 22
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Theme required but set to null since no styling for it
                        symbol[numberInstances] = context.getResources().getDrawable(symbolIndex[symbolSelected], null);
                    } else {
                        symbol[numberInstances] = context.getResources().getDrawable(symbolIndex[symbolSelected]);
                    }
                    symbol[numberInstances]
                            .setBounds(0, 0, symbolWidth[symbolSelected], symbolHeight[symbolSelected]);
                    instanceSelected = numberInstances;
                    numberInstances++;

                }

                // Drag the instance if selected (either an old instance or one just created)

                if (instanceSelected > -1) {
                    isDragging = true;
                    final float x = ev.getX();
                    final float y = ev.getY();

                    // Calculate the distance moved
                    final float dx = x - lastTouchX[instanceSelected];
                    final float dy = y - lastTouchY[instanceSelected];

                    // Move the object selected. Note that we are simply
                    // illustrating how to drag symbols. In an actual application,
                    // you would probably want to add some logic to confine the symbols
                    // to a region the size of the visible stage or smaller.

                    X[instanceSelected] += dx;
                    Y[instanceSelected] += dy;

                    // Remember this touch position for the next move event of this object
                    lastTouchX[instanceSelected] = x;
                    lastTouchY[instanceSelected] = y;

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
        for (int i = 0; i < numberInstances; i++) {
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

        // Draw image of symbols at their original locations to denote source

        for (int i = 0; i < numberSymbols; i++) {
            canvas.save();
            canvas.translate(X0[i], Y0[i]);
            symbol0[i].draw(canvas);
            canvas.restore();
        }

        // If dragging a symbol, display its instance number and x and y coordinates as dragged
        if (isDragging) {
            paint.setColor(TEXT_COLOR);
            canvas.drawText("Instance " + instanceSelected,
                    MainActivity.screenWidth/2,
                    MainActivity.topMargin/2 - 50, paint);
            canvas.drawText("X = " + X[instanceSelected],
                    MainActivity.screenWidth/2,
                    MainActivity.topMargin/2 + 0, paint);
            canvas.drawText("Y = " + Y[instanceSelected],
                    MainActivity.screenWidth/2,
                    MainActivity.topMargin/2 + 50, paint);
        }
    }
}
