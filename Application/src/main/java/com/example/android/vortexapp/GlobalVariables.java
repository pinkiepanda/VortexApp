package com.example.android.vortexapp;

import android.app.Application;

/**
 * Created by Yesung on 11/29/2016.
 */

public class GlobalVariables extends Application{
    private libsvm.svm_model myModel;
    public libsvm.svm_model getMyModel(){
        return myModel;
    }
    public void setMyModel(libsvm.svm_model model){
        myModel = model;
    }

    private libsvm.svm_parameter myParameter;
    public libsvm.svm_parameter getMyParameter(){
        return myParameter;
    }
    public void setState(libsvm.svm_parameter model){
        myParameter = model;
    }

    private libsvm.svm_problem myProblem;
    public libsvm.svm_problem getMyProblem(){
        return myProblem;
    }
    public void setMyProblem(libsvm.svm_problem model){
        myProblem = model;
    }

}
