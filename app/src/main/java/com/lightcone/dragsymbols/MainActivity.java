package com.lightcone.dragsymbols;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Dragger";

    // Screen dimensions and positioning offsets
    public static int screenWidth;
    public static int screenHeight;
    public static int topMargin = 0;
    private static final int xoff = 2;
    private static final int yoff = 2;
    private static final int xgap = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        Log.i(TAG, "Screen width=" + screenWidth + " height=" + screenHeight);

        // Put the reference to the symbols to be used in an array.
        // The Drawable corresponds to the symbol. R.drawable.file refers to
        // file.png, .jpg, or .gif stored in res/drawable (referenced
        // from code without the extension).

        int[] symbolIndex = { R.drawable.red_square, R.drawable.green_square,
                R.drawable.yellow_square };

        int numberSymbols = symbolIndex.length; // Total number of symbols to use
        int[] symbolWidth = new int[numberSymbols]; // Width of symbol in pixels
        int[] symbolHeight = new int[numberSymbols]; // Height of symbol in pixels

        // Determine the height and width of the symbols for positioning issues

        for (int i = 0; i < numberSymbols; i++) {

            // getDrawable(int id) is deprecated as of API version 22 in favor of
            // getDrawable(int id, Theme theme).  See
            // https://developer.android.com/reference/android/content/res/Resources.html.
            // Handle as follows.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                symbolWidth[i] = this.getResources().getDrawable(symbolIndex[i],this.getTheme())
                        .getIntrinsicWidth();
                symbolHeight[i] = this.getResources().getDrawable(symbolIndex[i], this.getTheme())
                        .getIntrinsicHeight();
            } else {
                symbolWidth[i] = this.getResources().getDrawable(symbolIndex[i])
                        .getIntrinsicWidth();
                symbolHeight[i] = this.getResources().getDrawable(symbolIndex[i])
                        .getIntrinsicHeight();
            }

            Log.i(TAG, "Symbol width=" + symbolWidth[i] + " height=" + symbolHeight[i]);

            // Set top margin (header) area equal to height of tallest symbol
            if (topMargin < symbolHeight[i]) topMargin = symbolHeight[i];
        }

        // Initial location of symbols. Coordinates are measured from the upper
        // left corner of the screen, with x increasing to the right and y
        // increasing downward.

        // Initial x coord in pixels for upper left corner of symbol
        float[] X = new float[numberSymbols];
        X[0] = xoff;
        X[1] = xoff + xgap + symbolWidth[0];
        X[2] = xoff + 2 * xgap + symbolWidth[0] + symbolWidth[1];

        // Initial y coord in pixels for upper left corner of symbol
        float[] Y = new float[numberSymbols];
        Y[0] = Y[1] = Y[2] = yoff;

       /*
       * Instantiate a SymbolDragger instance (which subclasses View), passing
       * to it in the constructor the context (this) and the above arrays.
       * Then set the content view to this instance of SymbolDragger (so the
       * layout is being specified entirely by SymbolDragger, with no XML
       * layout file). The resulting view should then place draggable symbols
       * with initial content and position defined by the above arrays on the
       * screen.
       */

        DragginSlayer view = new DragginSlayer(this, X, Y, symbolIndex);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(view);
    }
}
