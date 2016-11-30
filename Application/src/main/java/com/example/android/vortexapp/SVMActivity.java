package com.example.android.vortexapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonToken;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVMActivity extends Activity {

    String systemPath;// = MainActivity.getAppContext().getFilesDir().getPath() + "/";
    String fulldataTrainPath;// = systemPath + MainActivity.dataTrainPath;
    String fulldataPredictPath;// = systemPath + MainActivity.dataPredictPath;
    String fullmodelPath;// = systemPath + MainActivity.modelPath;
    String fulloutputPath;// = systemPath + MainActivity.outputPath;

    // link jni library
    static {
        System.loadLibrary("jnilibsvm");
    }

    // connect the native functions
    private native void jniSvmTrain(String cmd);
    private native void jniSvmPredict(String cmd);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        systemPath = MainActivity.getAppContext().getFilesDir().getPath() + "/";
        fulldataTrainPath = systemPath + MainActivity.dataTrainPath;
        fulldataPredictPath = systemPath + MainActivity.dataPredictPath;
        fullmodelPath = systemPath + MainActivity.modelPath;
        fulloutputPath = systemPath + MainActivity.outputPath;

        boolean worked = trainProblem();
        msg("In the end it worked: " + worked);

    }

    public svm_problem loadSVMProblem(){
        File tempPath = MainActivity.getAppContext().getFilesDir();
        File datafile = new File(tempPath,MainActivity.dataTrainPath+".txt");
        if (!datafile.exists())
        {
            msg("Datafile doesn't exist 1");
            return null;
        }
        BufferedReader testbr;
        try{
            testbr = new BufferedReader(new FileReader(datafile));
        }catch(FileNotFoundException fnfe){
            msg("Datafile doesn't exist 2");
            return null;
        }catch (IOException ioe){
            msg("IO Exception 1");
            return null;
        }

        /* need to count number of data lines / FSR sensors to initialize nodes array */
        String templine;
        int numrows = 0, numcols = 0;
        while (true){
            try{
                templine = testbr.readLine();
            }catch(IOException ioe){
                return null;
            }
            if(templine == null){
                break;
            }
            if(numrows == 0){
                String[] substrings = templine.trim().split(" ");
                for(int i = 1; i < substrings.length; i++){
                    String[] temp = substrings[i].split(":");
                    numcols++;
                }
            }
            numrows++;
        }

        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(datafile));
        }catch(FileNotFoundException fnfe){
            msg("Datafile doesn't exist 2");
            return null;
        }catch (IOException ioe){
            msg("IO Exception 1");
            return null;
        }
        svm_problem problem = new svm_problem();
        int row = 0;
        svm_node[][] nodes = new svm_node[numrows][numcols];
        double[] classes = new double[numrows];
        String line;
        while (true){
            try{
                line = br.readLine();
            }catch(IOException ioe){
                msg("IO Exception 2");
                return null;
            }
            if(line == null){
                msg("broke loop");
                break;
            }
            //msg(line);
            String[] substrings = line.trim().split(" ");
            //msg(substrings[0] + substrings [1] + substrings [2]);
            double y = Double.parseDouble(substrings[0].trim());
            //msg("double: " + y);

            List<svm_node> nodesrowList = new ArrayList<svm_node>();
            for(int i = 1; i < substrings.length; i++){
                String[] temp = substrings[i].split(":");
                //for (String tempp:temp) {
                 //   msg(tempp);
                //}
                svm_node node = new svm_node();
                node.index = Integer.parseInt(temp[0].trim());
                node.value = Integer.parseInt(temp[1].trim());
                nodesrowList.add(node);
            }
            int col = 0;
            svm_node[] noderow = new svm_node[nodesrowList.size()];
            for (svm_node tempnode:nodesrowList){
                noderow[col] = tempnode;
                col++;
            }
            nodes[row] = noderow;
            classes[row] = y;
            row++;
        }
        problem.x = nodes;
        problem.y = classes;
        problem.l = numrows;
        msg("got to the end?");
        return problem;
    }

    public String processData(String rawdataline){
        String stringtoreturn = "null";
        String cleanedrawdataline = rawdataline.substring(2);
        JSONObject jObject = null;
        JSONArray jArray = null;
        JsonToken jToken = null;
        boolean JSONValid = true, JarrayValid = true;
        try{
            jObject = new JSONObject(cleanedrawdataline);
        }catch (JSONException jex){
            JSONValid = false;
            stringtoreturn = "jobject failed";
        }
        if(JSONValid){
            try{
                //jToken = jObject.
                jArray = jObject.getJSONArray("fsr");
                //stringtoreturn = jArray.toString();
            }catch (JSONException jex){
                JarrayValid = false;
                stringtoreturn = "jarray failed";
            }
        }
        if(JarrayValid){
            stringtoreturn = "";
            int length = jArray.length();
            for(int i = 1; i <= length; i++){
                int num = jArray.optInt(i-1);
                stringtoreturn += i + ":" + num + " ";
            }
            //int[] nums = Integer.parseInt(jArray)
        }
        return stringtoreturn;
    }

    public boolean trainProblem(){
        boolean worked = true;
        svm svmClass = new libsvm.svm();

        svm_parameter param = new svm_parameter();
        param.C = 1000;
        param.gamma = 0.01;


        try{
            /*svm_problem problem = new svm_problem();
            problem = loadSVMProblem();
            double[] testing = problem.y;
            for(double y:testing){
                msg("y value: " + y + " l value: " + problem.l);
            }
            String error_msg = svmClass.svm_check_parameter(problem, param);
            if (error_msg != null) {
                System.err.print("ERROR: " + error_msg + "\n");
            }


            svm_model model = new svm_model();
            model = svmClass.svm_train(problem,param);*/
            String svmTrainOptions = "-t 2";
            jniSvmTrain(svmTrainOptions+" "+fulldataTrainPath+".txt "+fullmodelPath+".txt");
        }catch(Exception e){
            worked = false;
        }
        return worked;

    }

    // ----- from AndroidLibSVM by yctung (jnilibsvm library source) ------
    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
}
