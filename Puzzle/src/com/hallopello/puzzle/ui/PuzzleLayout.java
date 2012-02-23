package com.hallopello.puzzle.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.hallopello.puzzle.PuzzleActivity;

public class PuzzleLayout extends ViewGroup {

	private int rows = 4;
	private int cols = 4;
	private boolean initialLayout = true;
	
	private Piece[][] pieces;
	private ArrayList<Piece> normal = new ArrayList<Piece>(rows * cols);
	
	private BitmapDrawable image;
	
	public PuzzleLayout(Context context) {
		super(context);
		setup(context);
	}
	
	public PuzzleLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context);
	}

	public PuzzleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}
	
	/**
	 * Creates a new Bitmap that is scaled to fit in the screen.
	 */
	@Override
	public void setBackgroundDrawable(Drawable d) {
		super.setBackgroundDrawable(null);
		if (image != null) {
			image.getBitmap().recycle();
			image = null;
		}
		if (d != null) {
			BitmapDrawable bd = (BitmapDrawable)d;
			Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			Bitmap scaled = Bitmap.createScaledBitmap(bd.getBitmap(), display.getWidth(), display.getHeight(), false);
			image = new BitmapDrawable(getContext().getResources(), scaled);
			initialLayout = true;
			requestLayout();
			
			bd.getBitmap().recycle();
			bd = null;			
		}
	}
	
	/**
	 * Creates the array of pieces.
	 * @param context
	 */
	private void setup(Context context) {
		pieces = new Piece[rows][cols];
		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				Piece piece = new Piece(context);
				pieces[r][c] = piece;
				addView(piece);
			}
		}
	}
	
	/**
	 * This is the place where the Pieces get laid out and drawn.
	 * In the initialLayout, chop up the bitmap and set the section of Bitmap to a Piece. 
	 * Then mix up the pieces and redraw them mixed up.
	 */
	@Override
	protected void onLayout(boolean arg0, int l, int t, int rt, int b) {
		if (initialLayout) {
			cleanupBitmaps();
			for (int r=0; r<rows; r++) {
				for (int i=0; i<cols; i++) {
					Piece piece = pieces[r][i];
					normal.add(piece);
					if (image instanceof BitmapDrawable) {
						BitmapDrawable bd = (BitmapDrawable)image;
						Bitmap subset = Bitmap.createBitmap(bd.getBitmap(), i*(rt-l)/cols, r*(b-t)/rows, (i*(rt-l)/cols)+(rt-l)/cols - i*(rt-l)/cols, (r*(b-t)/rows)+(b-t)/rows - r*(b-t)/rows);
						if (subset != null) {
							piece.setBackgroundDrawable(new BitmapDrawable(getContext().getResources(), subset));
						}
					}
					piece.setBounds(new Rect(i*(rt-l)/cols, r*(b-t)/rows, (i*(rt-l)/cols)+(rt-l)/cols, (r*(b-t)/rows)+(b-t)/rows));
				}
			}
			image = null;
			mixupPieces();
			initialLayout = false;
			setBackgroundDrawable(null);
		}
		Piece selected = null;
		// Find the Piece that is being dragged
		for (int i=0; i<normal.size(); i++) {
			Piece piece = normal.get(i);
			if (piece.isSelected()) {
				selected = piece;
			}
		}
		int correct = 0;
		for (int i=0; i<normal.size(); i++) {
			Piece piece = normal.get(i);
			if (!piece.isSelected()) {
				if (selected != null && selected.isOverlapping(piece)) {
					piece.setHighlighted(true);
				} else {
					piece.setHighlighted(false);
				}
				piece.layout();
			}
			if (piece.isCorrectPosition()) {
				correct++;
			}
		}
		// Make sure the dragged one is on top.
		if (selected != null) {				
			selected.bringToFront();
			selected.layout();
		}
		// If the number of correct pieces equals the number of pieces 
		if (correct == normal.size()) {
			showPicker();
		}
	}
	
	/** 
	 * Send intent back to parent activity to show picker.
	 */
	private void showPicker() {
		Intent intent = new Intent(PuzzleActivity.SHOW_PICKER);
		getContext().sendBroadcast(intent);
	}
	
	/**
	 * Put each piece in a random place. Add all the bounds to an ArrayList, then
	 * randomly pick a piece to assign each bounds to.
	 */
	private void mixupPieces() {
		int size = normal.size();
		ArrayList<Rect> bounds = new ArrayList<Rect>(size);
		for (int i=0; i<size; i++) {
			bounds.add(normal.get(i).getBounds());
		}
		
		for (int i=0; i<size; i++) {
			int randNum = (int)Math.floor(Math.random() * bounds.size());
			Rect random = bounds.get(randNum);
			normal.get(i).setBounds(random);
			bounds.remove(randNum);
			
		}
		bounds = null;
	}
	
	/**
	 * Cleaning up Bitmaps for recycling.
	 */
	private void cleanupBitmaps() {
		if (normal != null) {
			for (int i=0; i<normal.size(); i++) {
				Drawable bg = normal.get(i).getBackground();
				if (bg instanceof BitmapDrawable) {
					((BitmapDrawable)bg).getBitmap().recycle();
					normal.get(i).setBackgroundDrawable(null);
				}
			}
			normal.clear();
			System.gc();
		}
	}
}
