package com.example.a3dobjectdetection.Tools;

import org.opencv.core.Mat;

public class MatrixTool {

    //蛮力法求解两个nxn和nxn阶矩阵相乘
    public static double[][] BruteForce(double[][] matrix1,double[][] matrix2){
        double[][] result=new double[matrix1.length][matrix2[0].length];
        for(int i=0;i<matrix1.length;i++) {
            for(int j=0;j<matrix2[0].length;j++) {
                  result[i][j] = 0;
                  for(int k=0;k<matrix2.length;k++) {
                      result[i][j] += matrix1[i][k] * matrix2[k][j];
                  }
                  //System.out.print("\t" + sum);
            }
            //System.out.println();
        }
        return result;
    }

    public static double[][] matTodoubletwo(Mat mat){
        double[] dd=new double[mat.rows()*mat.cols()*1];
        mat.get(0,0,dd);
        double[][] transpose=new double[mat.rows()][mat.cols()];
        for(int i=0;i<mat.rows();i++){
            for(int j=0;j<mat.cols();j++){
                transpose[i][j]=dd[i*mat.cols()+j];
            }
        }
        return transpose;
    }

    public static double[] matTodouble(Mat mat){
        double[] dd=new double[mat.rows()*mat.cols()*1];
        mat.get(0,0,dd);
        return dd;
    }
}
