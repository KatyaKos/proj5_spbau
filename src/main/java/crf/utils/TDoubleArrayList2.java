package crf.utils;

import gnu.trove.list.array.TDoubleArrayList;

public class TDoubleArrayList2 {

    private TDoubleArrayList[][] array;
    private final int LABELS_NUMBER = Labels.getInstance().getLabelsSize();
    private final int n;
    private final int m;
    private final int k;

    public TDoubleArrayList2(int n, int m, int k,  double val) {
        this.n = n;
        this.m = m;
        this.k = k;
        cleanData(val);
    }

    private void cleanData(double val) {
        array = new TDoubleArrayList[n][m];
        TDoubleArrayList tmp = fillTDouble(k, val);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                array[i][j] = new TDoubleArrayList(tmp);
    }

    public double get(int i, int j, int k) {
        return array[i][j].get(k);
    }

    public void set(int i, int j, int k, double val) {
        array[i][j].set(k, val);
    }

    public static TDoubleArrayList fillTDouble(int size, double value) {
        double[] tmp = new double[size];
        for (int i = 0; i < size; i++) tmp[i] = value;
        return new TDoubleArrayList(tmp);
    }
}
