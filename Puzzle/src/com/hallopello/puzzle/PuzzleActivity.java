package com.hallopello.puzzle;

import java.io.FileInputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
//import android.widget.Toast;

import com.hallopello.puzzle.ui.PuzzleLayout;

public class PuzzleActivity extends Activity {
	
    private static final int PICK_IMAGE = 0;
    
    public static final String CREATE_NEW_PUZZLE = "com.hallopello.puzzle.CREATE_NEW_PUZZLE";
    public static final String SHOW_PICKER = "com.hallopello.puzzle.SHOW_PICKER";
    
    private PuzzleLayout puzzle;
    private Button picker;
    
    private BroadcastReceiver receiver = new BroadcastReceiver() {
    	@Override
		public void onReceive(Context context, Intent intent) {
			if (CREATE_NEW_PUZZLE.equals(intent.getAction())) {
				createNewPuzzle();
			}
			else if (SHOW_PICKER.equals(intent.getAction())) {
				showPicker();
			}
		}
    };

	/** 
	 * Set the view layout and start the first puzzle.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        puzzle = (PuzzleLayout)findViewById(R.id.puzzle);
        picker = (Button)findViewById(R.id.picknext);
        
        createNewPuzzle();
    }
    
    /** 
     * Register intent filters here.
     */
    @Override
    protected void onStart() {
    	super.onStart();
    	registerReceiver(receiver, new IntentFilter(CREATE_NEW_PUZZLE));
        registerReceiver(receiver, new IntentFilter(SHOW_PICKER));        
    }
    
    /**
     * Unregister receivers for the intent filters.
     */
    @Override
    protected void onStop() {
    	unregisterReceiver(receiver);
    	super.onStop();
    }
    
    /**
     * This method sets the picker button to be visible.
     */
    private void showPicker() {
    	picker.setVisibility(View.VISIBLE);
    }
    
    /**
     * Create a new random puzzle from an image in the gallery.
     */
    public void createNewPuzzle() {        
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.Images.Media.MIME_TYPE + " = ?", new String[] { "image/jpeg" },null);
        cursor.moveToPosition((int)(Math.random() * cursor.getCount()));
       
        loadBitmap(cursor);
    }
    
    /** 
     * Add a menu item.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(getResources().getString(R.string.pick_another));
    	return true;
    }
    
    /**
     * When your menu item is selected, start the gallery intent.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (getResources().getString(R.string.pick_another).equals(item.getTitle())) {
    		showGallery(); 
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    /**
     * The return from picking something from the gallery.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
    		Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            loadBitmap(cursor);
    	}
    }
    
    /**
     * Load in the image from the gallery.
     * @param cursor
     */
    private void loadBitmap(Cursor cursor) {
    	int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        try {
        	BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[32 * 1024];
            FileInputStream fis = new FileInputStream(filePath);
        	puzzle.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options)));
        }
        catch (Throwable t) {
        	//Toast.makeText(this, "Error reading file " + filePath, 3000).show();
        	Log.e("puzzle", "whoops", t);
        }
    }
    
    /**
     * Start the gallery intent.
     */
    private void showGallery() {
    	Intent intent = new Intent(Intent.ACTION_PICK,
	               android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, PICK_IMAGE); 
    }
    
    /**
     * The onclick method for the picker button.
     * @param button
     */
    public void pickNext(View button) {
    	button.setVisibility(View.GONE);
    	showGallery();
    }
}