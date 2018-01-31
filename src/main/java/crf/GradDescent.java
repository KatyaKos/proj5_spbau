package crf;

import crf.utils.*;
import crf.utils.Attributes.Attribute;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.List;

import static crf.utils.Constants.GRAD_DESC_COEF;

public class GradDescent {

    private List<TIntArrayList> hiddens;
    private List<Attributes> observations;
    private TIntArrayList hids = new TIntArrayList();
    private Attributes observs = new Attributes();

    private TrainCrf trainingModel;
    private final Coefficients coefficients;
    private final int LABELS_NUMBER = Labels.getInstance().getLabelsSize();
    private final int fNumber;
    private final int gNumber;
    private CrfFunctions crfFunctions = CrfFunctions.getInstance();

    GradDescent(TrainCrf train, List<TIntArrayList> hiddens, List<Attributes> observations) {
        trainingModel = train;
        this.hiddens = hiddens;
        this.observations = observations;
        coefficients = train.coefficients;
        fNumber = train.fNumber;
        gNumber = train.gNumber;
        modulesLambda =  new TDoubleArrayList();
        modulesMu = new TDoubleArrayList();
    }

    private TDoubleArrayList[][] countedDlambdaFuncSum;
    private double dLambdaFuncSum(int t, int y_val, int k) {
        if (countedDlambdaFuncSum[t][k].get(y_val) != -1d) return countedDlambdaFuncSum[t][k].get(y_val);
        double res = 0d;
        if (t == 0) {
            countedDlambdaFuncSum[t][k].set(y_val, 0d);
            return 0d;
        }
        Attribute attr = observs.getAttribute(t);
        for (int y = 1; y < LABELS_NUMBER; y++) {
            res += crfFunctions.getFFunction(k, y, y_val, attr) * trainingModel.countProbability(t - 1, y);
        }
        for (int l = 0; l < fNumber; l++) {
            double dpfSum = 0d;
            for (int y = 1; y < LABELS_NUMBER; y++) {
                dpfSum += crfFunctions.getFFunction(l, y, y_val, attr) * dLambdaProbFunc(t - 1, y, k);
            }
            res += coefficients.getLambda(l) * dpfSum;
        }
        countedDlambdaFuncSum[t][k].set(y_val, res);
        return res;
    }

    private TDoubleArrayList[][] countedDmuFuncSum;
    private double dMuFuncSum(int t, int y_val, int k) {
        if (countedDmuFuncSum[t][k].get(y_val) != -1d) return countedDmuFuncSum[t][k].get(y_val);
        double res = 0d;
        if (t == 0) {
            countedDmuFuncSum[t][k].set(y_val, 0d);
            return 0d;
        }
        Attribute attr = observs.getAttribute(t);
        for (int l = 0; l < fNumber; l++) {
            double dpfSum = 0d;
            for (int y = 1; y < LABELS_NUMBER; y++) {
                dpfSum += crfFunctions.getFFunction(l, y, y_val, attr) * dMuProbFunc(t - 1, y, k);
            }
            res += coefficients.getLambda(l) * dpfSum;
        }
        res += crfFunctions.getGFunction(k, y_val, attr);
        countedDmuFuncSum[t][k].set(y_val, res);
        return res;
    }

    private TDoubleArrayList[][] countedDlambdaP;
    private double dLambdaProbFunc(int t, int y_val, int k) {
        if (countedDlambdaP[t][k].get(y_val) != -1d) return countedDlambdaP[t][k].get(y_val);
        if (t == 0) {
            countedDlambdaP[t][k].set(y_val, 0d);
            return 0d;
        }
        double res = 0d;
        for (int y = 1; y < LABELS_NUMBER; y++) {
            res += trainingModel.countProbability(t, y) * (dLambdaFuncSum(t, y_val, k) - dLambdaFuncSum(t, y, k));
        }
        res *= trainingModel.countProbability(t, y_val);
        countedDlambdaP[t][k].set(y_val, res);
        return res;
    }

    private TDoubleArrayList[][] countedDmuP;
    private double dMuProbFunc(int t, int y_val, int k) {
        if (countedDmuP[t][k].get(y_val) != -1d) return countedDmuP[t][k].get(y_val);
        if (t == 0) {
            countedDmuP[t][k].set(y_val, 0d);
            return 0d;
        }
        double res = 0d;
        for (int y = 1; y < LABELS_NUMBER; y++) {
            res += trainingModel.countProbability(t, y) * (dMuFuncSum(t, y_val, k) - dMuFuncSum(t, y, k));
        }
        res *= trainingModel.countProbability(t, y_val);
        countedDmuP[t][k].set(y_val, res);
        return res;
    }


    private TDoubleArrayList dlambda;
    private void dLambdaFunc() {
        for (int k = 0; k < fNumber; k++) {
            double res = 0d;
            for (int t = 1; t < hids.size(); t++) {
                final int y_val = hids.get(t);
                double dpFuncSum = 0d;
                for (int y = 1; y < LABELS_NUMBER; y++) {
                    dpFuncSum += trainingModel.countProbability(t, y) * dLambdaFuncSum(t, y, k);
                }
                res += dLambdaFuncSum(t, y_val, k) - dpFuncSum;
            }
            dlambda.set(k, dlambda.get(k) + res);
        }
    }

    private TDoubleArrayList dmu;
    private void dMuFunc() {
        for (int k = 0; k < gNumber; k++) {
            double res = 0d;
            for (int t = 1; t < hids.size(); t++) {
                final int y_val = hids.get(t);
                double dpFuncSum = 0d;
                for (int y = 1; y < LABELS_NUMBER; y++) {
                    dpFuncSum += trainingModel.countProbability(t, y) * dMuFuncSum(t, y, k);
                }
                res += dMuFuncSum(t, y_val, k) - dpFuncSum;
            }
            dmu.set(k, dmu.get(k) + res);
        }
    }

    private void cleanCountedData() {
        countedDlambdaP = new TDoubleArrayList[observs.size()][fNumber];
        TDoubleArrayList temp = CrfImpl.fillTDouble(LABELS_NUMBER, -1d);
        for (int i = 0; i < observs.size(); i++)
            for (int j = 0; j < fNumber; j++)
                countedDlambdaP[i][j] = new TDoubleArrayList(temp);

        countedDmuP = new TDoubleArrayList[observs.size()][gNumber];
        temp = CrfImpl.fillTDouble(LABELS_NUMBER, -1d);
        for (int i = 0; i < observs.size(); i++)
            for (int j = 0; j < gNumber; j++)
                countedDmuP[i][j] = new TDoubleArrayList(temp);


        countedDlambdaFuncSum = new TDoubleArrayList[observs.size()][fNumber];
        temp = CrfImpl.fillTDouble(LABELS_NUMBER, -1d);
        for (int i = 0; i < observs.size(); i++)
            for (int j = 0; j < fNumber; j++)
                countedDlambdaFuncSum[i][j] = new TDoubleArrayList(temp);

        countedDmuFuncSum = new TDoubleArrayList[observs.size()][gNumber];
        temp = CrfImpl.fillTDouble(LABELS_NUMBER, -1d);
        for (int i = 0; i < observs.size(); i++)
            for (int j = 0; j < gNumber; j++)
                countedDmuFuncSum[i][j] = new TDoubleArrayList(temp);
    }

    private void dCoefficientsFunc() {
        dlambda = CrfImpl.fillTDouble(fNumber, 0d);
        dmu = CrfImpl.fillTDouble(gNumber, 0d);
        for (int m = 0; m < hiddens.size(); m++) {
            hids = hiddens.get(m);
            observs = observations.get(m);
            trainingModel.initializeLabelsData(observs);
            cleanCountedData();
            dLambdaFunc();
            dMuFunc();
        }
    }

    TDoubleArrayList modulesLambda;
    TDoubleArrayList modulesMu;

    public void move() {
        dCoefficientsFunc();
        double norm = 0d;
        for (int i = 0; i < fNumber; i++) {
            norm += dlambda.get(i) * dlambda.get(i);
        }
        for (int i = 0; i < gNumber; i++) {
            norm += dmu.get(i) * dmu.get(i);
        }
        System.out.println("Grad norm: " + Math.sqrt(norm));
        for (int i = 0; i < coefficients.getLambdasNumber(); i++)
            coefficients.setLambda(i, coefficients.getLambda(i) + GRAD_DESC_COEF * dlambda.get(i));
        for (int i = 0; i < coefficients.getMusNumber(); i++)
            coefficients.setMu(i, coefficients.getMu(i) + GRAD_DESC_COEF * dmu.get(i));
    }
}
