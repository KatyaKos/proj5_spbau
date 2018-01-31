package crf.utils;

import crf.CrfImpl;
import gnu.trove.list.array.TDoubleArrayList;

public class Coefficients {
    private final int LAMBDAS_NUMBER;
    private final int MUS_NUMBER;

    private TDoubleArrayList mus;
    private TDoubleArrayList lambdas;

    public Coefficients(int lambdas, int mus) {
        LAMBDAS_NUMBER = lambdas;
        MUS_NUMBER = mus;
        this.mus = CrfImpl.fillTDouble(mus, 1d);
        this.lambdas = CrfImpl.fillTDouble(lambdas, 1d);
    }

    public double[] getAllLambdas() {
        return lambdas.toArray();
    }

    public double[] getAllMus() {
        return mus.toArray();
    }

    public int getLambdasNumber() {
        return LAMBDAS_NUMBER;
    }

    public int getMusNumber() {
        return MUS_NUMBER;
    }

    public double getLambda(int i) {
        return lambdas.get(i);
    }

    public double getMu(int i) {
        return mus.get(i);
    }

    public void setLambda(int i, double l) {
        lambdas.set(i, l);
    }

    public void setMu(int i, double m) {
        mus.set(i, m);
    }
}
