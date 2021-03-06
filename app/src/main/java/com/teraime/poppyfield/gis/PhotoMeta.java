package com.teraime.poppyfield.gis;

import android.util.Log;

import java.io.Serializable;

public class PhotoMeta implements Serializable {
    private static final long serialVersionUID = -3400543797668108399L;
    public double N=0,E=0,S=0,W=0;
    private boolean isValid = false;


    public PhotoMeta(String N,String E, String S, String W) {
        try {
            this.N = Double.parseDouble(N);
            this.W = Double.parseDouble(W);
            this.S = Double.parseDouble(S);
            this.E = Double.parseDouble(E);
            isValid = true;
        } catch (Exception e) { Log.e("vortex","non number in gis bg coordinates"); }


    }

    public PhotoMeta(double N,double E,double S,double W) {
        this.N=N;
        this.W=W;
        this.S=S;
        this.E=E;

    }

    public double getWidth()  {
        return E-W;
    }
    public double getHeight() {
        return N-S;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        return ("N: "+N+" W: "+W+" E:"+E+" S:"+S+" isValid: "+isValid+" Width: "+getWidth()+" Height: "+getHeight());
    }
}

