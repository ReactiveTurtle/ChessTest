package ru.reactiveturtle.test.chessneuron.chessengine;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import ru.reactiveturtle.test.chessneuron.R;

public class ChessboardView extends View {
    private Paint mPaint;
    private Matrix mTransformMatrix;
    private int[][] colors = new int[8][8];
    private Bitmap[][] bitmaps = new Bitmap[8][8];
    public static final int DEFAULT_FIRST_COLOR = Color.WHITE;
    public static final int DEFAULT_SECOND_COLOR = Color.GRAY;

    public ChessboardView(Context context) {
        super(context);
        init();
    }

    public ChessboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTransformMatrix = new Matrix();

        int firstColor = getFirstColor();
        int secondColor = getSecondColor();
        boolean isFirst = true;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                colors[i][j] = isFirst ? firstColor : secondColor;
                bitmaps[i][j] = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
                isFirst = !isFirst;
            }
            isFirst = !isFirst;
        }
    }


    public int[][] getColors() {
        return colors;
    }

    public void setCellPicture(Bitmap bitmap, int x, int y) {
        bitmaps[y][x] = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float sectorWidth = width / 8f;
        float sectorHeight = height / 8f;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                mPaint.setColor(colors[i][j]);
                canvas.drawRect(sectorWidth * j, sectorHeight * i, sectorWidth * (j + 1), sectorHeight * (i + 1), mPaint);
                mTransformMatrix.reset();
                mTransformMatrix.preScale((sectorWidth - 16) / bitmaps[i][j].getWidth(), (sectorHeight - 16) / bitmaps[i][j].getHeight());
                mTransformMatrix.postTranslate(sectorWidth * j + 8, sectorHeight * i + 8);
                canvas.drawBitmap(bitmaps[i][j], mTransformMatrix, mPaint);
            }
        }
    }

    public int getFirstColor() {
        return getAttrColor(R.styleable.ChessboardView_colorFirst, Color.WHITE);
    }

    public int getSecondColor() {
        return getAttrColor(R.styleable.ChessboardView_colorSecond, Color.BLACK);
    }

    private int getAttrColor(int attr, int defaultColor) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, defaultColor);

        a.recycle();
        return color;
    }

    private int getAttrInt(int attr) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getInt(0, 1);

        a.recycle();
        return color;
    }
}
