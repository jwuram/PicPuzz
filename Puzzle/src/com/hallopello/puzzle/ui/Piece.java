package com.hallopello.puzzle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class Piece extends View {

	private boolean isSelected = false;
	private int color = -1;
	private Rect bounds = null;
	private Rect original = null;
	private Rect correct = null;
	private float x=0, y=0;
	private boolean isHighlighted = false;
	private boolean isCorrect = false;
	
	private Piece overlap = null;
	
	public Piece(Context context) {
		super(context);
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
	/**
	 * Sets whether this piece is highlighted (being dragged over) or not.
	 * @param highlight
	 */
	public void setHighlighted(boolean highlight) {
		if (!isCorrect) {
			isHighlighted = highlight;
			invalidate();
		}
	}
	
	protected boolean isCorrectPosition() {
		return isCorrect;
	}
	
	@Override
	public void setBackgroundColor(int color) {
		if (this.color == -1) {
			this.color = color;
		}
		super.setBackgroundColor(color);
	}
	
	/**
	 * If it's not in the right place, draw a black border around it. 
	 * If it's in the right place, no border.
	 * If it's being dragged over, draw a thicker white border.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (!isCorrect) {
			Paint paint = new Paint();
			paint.setColor(0xff000000);
			paint.setStrokeWidth(0);
			paint.setStyle(Style.STROKE);
			canvas.drawRect(0, 0, getWidth()-1, getHeight()-1, paint);
		}
		
		if (isHighlighted) {
			Paint paint = new Paint();
			paint.setColor(0x77ffffff);
			paint.setStrokeWidth(10.0f);
			paint.setStyle(Style.STROKE);
			canvas.drawRect(0, 0, getWidth()-1, getHeight()-1, paint);
		}
	}
	
	/**
	 * Tracking the touch events on pieces.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN && !isCorrect) {
			isSelected = true;
			x = event.getRawX();
			y = event.getRawY();
			original = bounds;
			return true;
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			isSelected = false;
			if (overlap != null) {
				swapPlaces(overlap);
				getParent().requestLayout();
				overlap = null;
			} else {
				bounds = original;
				getParent().requestLayout();
			}
			return true;
		}
		if (isSelected) {
		    final int newX = getLeft() - (int)(x - event.getRawX());
	    	final int newY = getTop() - (int)(y - event.getRawY());

	    	bounds = new Rect(newX, newY, newX+getWidth(), newY+getHeight());
			
	    	x = event.getRawX();
	    	y = event.getRawY();
	    	  
	    	getParent().requestLayout();
			return true;
		}
		return false;
	}
	
	public Rect getBounds() {
		return bounds;
	}
	
	/**
	 * The PuzzleLayout class calculates the bounds for its children.
	 * @param bounds
	 */
	protected void setBounds(Rect bounds) {
		this.bounds = bounds;
		if (correct == null) {
			correct = bounds;
		} else {
			isCorrect = correct.equals(bounds);
			if (isCorrect && isHighlighted) {
				isHighlighted = false;
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (bounds == null) {
			bounds = new Rect(left, top, right, bottom);
		}
	}

	/**
	 * This sets the bounds for this piece.
	 */
	protected void layout() {
		if (bounds != null) {
			layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
		}
	}
	
	/**
	 * Checks if other overlaps this piece.
	 * @param other
	 * @return
	 */
	protected boolean isOverlapping(Piece other) {
		if (other.isCorrectPosition()) {
			return false;
		}
		boolean isOverlap = other.getBounds().contains(bounds.centerX(), bounds.centerY());
		if (isOverlap) {
			overlap = other;
		}
		return isOverlap;
	}
	
	/**
	 * Switch places with other.
	 * @param other
	 */
	protected void swapPlaces(Piece other) {
		Rect otherBounds = other.getBounds();
		other.setBounds(original);
		setBounds(otherBounds);
		
		getParent().requestLayout();
	}
}
