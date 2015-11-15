package com.isjctu.pulse;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Drew on 11/14/2015.
 */
public class SyncService extends Service {

    private static class PushToServerTask extends AsyncTask<ArrayList<HeartBeat>, Void, Integer> {

        Socket mySocket;

        @Override
        protected Integer doInBackground(ArrayList<HeartBeat>... params) {
            try {

                mySocket = new Socket("54.152.69.195", 6969);
                Log.e("Created Socket", "");
                String result = "false";
                if(mySocket.isConnected()) {
                    result = "true";
                }
                Log.e("Connection: ", result);
                OutputStream out = mySocket.getOutputStream();
                InputStream in = mySocket.getInputStream();
                ByteBuffer myBuffer = ByteBuffer.allocate(100);




                int numPoints = params[0].size();
                float avgRate = 0;
                double avgTime = 0;
                double avgLat = 0;
                double avgLon = 0;
                for(int i = 0; i < numPoints; i++) {
                    avgRate+=params[0].get(i).rate;
                    avgTime+=(double)params[0].get(i).time;
                    avgLat += params[0].get(i).lat;
                    avgLon += params[0].get(i).lon;
                }
                avgRate/=numPoints;
                avgTime/=numPoints;
                avgLat/=numPoints;
                avgLon/=numPoints;




                JSONArray jArr = new JSONArray();

                JSONObject innerJ = new JSONObject();
                innerJ.put("rate", avgRate);
                innerJ.put("time", avgTime);
                innerJ.put("lat", avgLat);
                innerJ.put("lon", avgLon);

                jArr.put(innerJ);

                JSONObject topObj = new JSONObject();

                topObj.put("requestType", "Send");
                topObj.put("dataSet", jArr);


                Log.e("JSON: ", topObj.toString());

                myBuffer.put(topObj.toString().getBytes("utf-8"));
                out.write(myBuffer.array());
                out.flush();
                Log.e("Success", "made it");
                byte[] feedback = new byte[100];
                in.read(feedback);
                ByteBuffer inBuffer = ByteBuffer.allocate(100);
                inBuffer.put(feedback);
                Log.e("Feedback: ", new String(inBuffer.array()));
                out.close();
                in.close();

            } catch (IOException e) {
                Log.e("Error", "Did not connect to server: " + e.getClass().getCanonicalName());
            } catch (JSONException e) {

            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            try {
                mySocket.close();
            } catch (Exception e) {

            }
            super.onPostExecute(integer);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final int size = 100;
        ArrayList<HeartBeat> myList = new ArrayList<>();

        //Get stuff in
        //Expect 3 arrays and a string
        float[] rateArr = new float[size]; //something in
        long[] timeArr = new long[size]; //something in
        ArrayList<Integer> accAL = new ArrayList<>();
        //String id = ""; //something in


        //Instantiate accAL
        for(int i=0; i<size; i++){
            accAL.add(i, 0);
        }


        ArrayList<Float> rateAL = new ArrayList<>();
        for(int i=0; i<size; i++ ){
            if(accAL.get(i) > 0) { // OR W/E
                rateAL.add(rateArr[i]);
            }
        }
        ArrayList<Long> timeAL = new ArrayList<>();
        for(int i=0; i<size; i++ ){
            if(accAL.get(i) > 0) { // OR W/E
                timeAL.add(timeArr[i]);
            }
        }



        //call method in main activity to populate arraylists

        HeartBeat curr;
        for(int i = 0; i < size; i++) {
            //curr = new HeartBeat(rateAL.get(i), timeAL.get(i), LONGITUDE, LATITUDE);
            curr = new HeartBeat(0, 0, 0, 0);
            myList.add(curr);
        }
        PushToServerTask myTask = new PushToServerTask();
        myTask.execute(myList);
        return START_NOT_STICKY;
    }

}
