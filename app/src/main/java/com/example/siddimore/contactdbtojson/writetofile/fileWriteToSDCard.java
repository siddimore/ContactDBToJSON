package com.example.siddimore.contactdbtojson.writetofile;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by siddimore on 9/21/16.
 */
 public class fileWriteToSDCard {

    static String fileName = "CompanionContact.json";
    public static String writeToSDCard(JSONArray inputArray) {

        try {
            // get the path to sdcard
            File sdcard = Environment.getExternalStorageDirectory();
            // to this path add a new directory path
            File dir = new File(sdcard.getAbsolutePath() + "/ContactdbToJson/");
            // create this directory if not already created
            dir.mkdir();
            // create the file in which we will write the contents
            File file = new File(dir, fileName);
            FileOutputStream os  = new FileOutputStream(file);
            String data = "This is the content of my file";
            os.write(data.getBytes());
            os.close();

            return dir+fileName;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Helper Method
    private static boolean canWriteOnExternalStorage() {
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            Log.d("FileWriteSDCard", "Yes, can write to external storage.");
            return true;
        }
        return false;
    }

}
