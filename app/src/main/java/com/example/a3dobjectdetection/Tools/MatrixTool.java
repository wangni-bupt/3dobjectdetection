package com.example.a3dobjectdetection.Tools;

import android.util.Log;

import com.google.ar.core.Pose;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MatrixTool {
    private final static  String TAG=MatrixTool.class.getSimpleName();

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

    public static float[][] BruteForce(float[][] matrix1,float[][] matrix2){
        float[][] result=new float[matrix1.length][matrix2[0].length];
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

    public static float[][] matTofloattwo(Mat mat){
        float[] dd=new float[mat.rows()*mat.cols()*1];
        mat.get(0,0,dd);
        float[][] transpose=new float[mat.rows()][mat.cols()];
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

    //64F ,而java中float为32位，所以不能用
    public static float[] matTofloat(Mat mat){
        float[] dd=new float[mat.rows()*mat.cols()*1];
        mat.get(0,0,dd);
        return dd;
    }

    public static void doubletofloat(double[] R,double[] t,float[] dd){
        dd[0]=(float) R[0];
        dd[1]=(float) R[1];
        dd[2]=(float) R[2];
        dd[3]=(float) t[0];
        dd[4]=(float) R[3];
        dd[5]=(float) R[4];
        dd[6]=(float) R[5];
        dd[7]=(float) t[1];
        dd[8]=(float) R[6];
        dd[9]=(float) R[7];
        dd[10]=(float) R[8];
        dd[11]=(float) t[2];
        dd[12]=0;
        dd[13]=0;
        dd[14]=0;
        dd[15]=1;
    }
    public static void floattofloat(float[] R,float[] t,float[] dd){
        dd[0]=(float) R[0];
        dd[1]=(float) R[1];
        dd[2]=(float) R[2];
        dd[3]=(float) t[0];
        dd[4]=(float) R[3];
        dd[5]=(float) R[4];
        dd[6]=(float) R[5];
        dd[7]=(float) t[1];
        dd[8]=(float) R[6];
        dd[9]=(float) R[7];
        dd[10]=(float) R[8];
        dd[11]=(float) t[2];
        dd[12]=0;
        dd[13]=0;
        dd[14]=0;
        dd[15]=1;
    }

    //取转置
    public static float[] floattomattofloat(float[] floats){
        Mat mat=new Mat(4,4, CvType.CV_32FC1);
        mat.put(0,0,floats);
        return MatrixTool.matTofloat(mat.t());
    }


    public static float[] rotationtoquen(float[] floats) {
        return fromRotationMatrix(floats[0], floats[1], floats[2], floats[4], floats[5],
                floats[6],floats[8], floats[9],floats[10]);
    }

    public static float[] fromRotationMatrix(float m00, float m01, float m02,
                                         float m10, float m11, float m12, float m20, float m21, float m22) {
        // first normalize the forward (F), up (U) and side (S) vectors of the rotation matrix
        // so that the scale does not affect the rotation
        float x,y,z,w=1;
        float lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / (float) Math.sqrt(lengthSquared);
            m00 *= lengthSquared;
            m10 *= lengthSquared;
            m20 *= lengthSquared;
        }
        lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / (float) Math.sqrt(lengthSquared);
            m01 *= lengthSquared;
            m11 *= lengthSquared;
            m21 *= lengthSquared;
        }
        lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / (float) Math.sqrt(lengthSquared);
            m02 *= lengthSquared;
            m12 *= lengthSquared;
            m22 *= lengthSquared;
        }

        // Use the Graphics Gems code, from
        // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
        // *NOT* the "Matrix and Quaternions FAQ", which has errors!

        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        float t = m00 + m11 + m22;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = (float) Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s;                 // so this division isn't bad
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            float s = (float) Math.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            float s = (float) Math.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            float s = (float) Math.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }

        return new float[]{x,y,z,w};

    }


    //矩阵以行形式
    public static float[] gettranslate(float[] floats){
        return new float[]{floats[3],floats[7],floats[11]};
    }

    public static float[] getRotation(float[] floats){
        return new float[]{floats[0],floats[1],floats[2],
                floats[3],floats[4],floats[5],
                floats[6],floats[7],floats[8]};
    }
    //矩阵以列形式
    public static float[] gettranslate2(float[] floats){
        return new float[]{floats[12],floats[13],floats[14]};
    }

//    public static float[] getRotation2(float[] floats){
//        return new float[]{floats[0],floats[1],floats[2],
//                floats[3],floats[4],floats[5],
//                floats[6],floats[7],floats[8]};
//    }

    //有问题
    public static float[] queutoMatrix(float[] Quaternion)
    {
        float[] floats=new float[9];
        floats[0] = 1 - 2 * (Quaternion[2] * Quaternion[2]) - 2 * (Quaternion[3] * Quaternion[3]);
        floats[1] = 2 * Quaternion[1] * Quaternion[2] - 2 * Quaternion[0] * Quaternion[3];
        floats[2] = 2 * Quaternion[1] * Quaternion[3] + 2 * Quaternion[0] * Quaternion[2];
        floats[3] = 2 * Quaternion[1] * Quaternion[2] + 2 * Quaternion[0] * Quaternion[3];
        floats[4] = 1 - 2 * (Quaternion[1] * Quaternion[1]) - 2 * (Quaternion[3] * Quaternion[3]);
        floats[5] = 2 * Quaternion[2] * Quaternion[3] - 2 * Quaternion[0] * Quaternion[1];
        floats[6] = 2 * Quaternion[1] * Quaternion[3] - 2 * Quaternion[0] * Quaternion[2];
        floats[7] = 2 * Quaternion[2] * Quaternion[3] + 2 * Quaternion[0] * Quaternion[1];
        floats[8] = 1 - 2 * (Quaternion[1] * Quaternion[1]) - 2 * (Quaternion[2] * Quaternion[2]);
        return floats;
    }

}
