package crf;

import crf.utils.*;
import crf.utils.Attributes.Attribute;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.List;

import static crf.utils.Constants.GRAD_DESC_COEF;
import static crf.utils.TDoubleArrayList2.fillTDouble;

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
    private final DerivativeFunction derivativeFunction = new DerivativeFunction();

    GradDescent(TrainCrf train, List<TIntArrayList> hiddens, List<Attributes> observations) {
        trainingModel = train;
        this.hiddens = hiddens;
        this.observations = observations;
        coefficients = train.coefficients;
        fNumber = train.fNumber;
        gNumber = train.gNumber;
        dmu = fillTDouble(gNumber, 0d);
        dlambda = fillTDouble(fNumber, 0d);
    }

    public void move() {
        dFunc();
        final double norm = norm();
        System.out.println("Grad norm: " + Math.sqrt(norm));
        moveCoefficients();
    }

    private void moveCoefficients() {
        for (int i = 0; i < coefficients.getLambdasNumber(); i++)
            coefficients.setLambda(i, coefficients.getLambda(i) + GRAD_DESC_COEF * dlambda.get(i));
        for (int i = 0; i < coefficients.getMusNumber(); i++)
            coefficients.setMu(i, coefficients.getMu(i) + GRAD_DESC_COEF * dmu.get(i));
    }

    private double norm() {
        double norm = 0d;
        for (int i = 0; i < fNumber; i++) {
            norm += dlambda.get(i) * dlambda.get(i);
        }
        for (int i = 0; i < gNumber; i++) {
            norm += dmu.get(i) * dmu.get(i);
        }
        return norm;
    }

    private void dFunc() {
        dlambda = fillTDouble(fNumber, 0d);
        dmu = fillTDouble(gNumber, 0d);
        for (int m = 0; m < hiddens.size(); m++) {
            hids = hiddens.get(m);
            observs = observations.get(m);
            trainingModel.initializeLabelsData(observs);
            derivativeFunction.cleanCountedData();
            dLambdaFunc();
            dMuFunc();
        }
    }

    private TDoubleArrayList dlambda;
    private void dLambdaFunc() {
        dCoefFunc(1, dlambda);
    }

    private TDoubleArrayList dmu;
    private void dMuFunc() {
        dCoefFunc(2, dmu);
    }

    private void dCoefFunc(int type, TDoubleArrayList array) {
        for (int k = 0; k < array.size(); k++) {
            double res = 0d;
            for (int t = 1; t < hids.size(); t++) {
                final int y_val = hids.get(t);
                double dpFuncSum = 0d;
                for (int y = 1; y < LABELS_NUMBER; y++) {
                    dpFuncSum += trainingModel.countProbability(t, y) * derivativeFunction.execute(type, t, y, k);
                }
                res += derivativeFunction.execute(type, t, y_val, k) - dpFuncSum;
            }
            array.set(k, array.get(k) + res);
        }
    }

    private class DerivativeFunction {

        double execute(int type, int t, int y, int k) {
            assert type >= 1 && type <= 4;
            switch (type) {
                case 1:
                    return dLambdaFuncSum(t, y, k);
                case 2:
                    return dMuFuncSum(t, y, k);
                case 3:
                    return dLambdaProbFunc(t, y, k);
                case 4:
                    return dMuProbFunc(t, y, k);
                default:
                    return 0d;
            }
        }

        void cleanCountedData() {
            countedDlambdaP = new TDoubleArrayList2(observs.size(), fNumber, LABELS_NUMBER, -1d);
            countedDmuP = new TDoubleArrayList2(observs.size(), gNumber, LABELS_NUMBER, -1d);
            countedDlambdaFuncSum = new TDoubleArrayList2(observs.size(), fNumber, LABELS_NUMBER, -1d);
            countedDmuFuncSum = new TDoubleArrayList2(observs.size(), gNumber, LABELS_NUMBER, -1d);
        }

        private TDoubleArrayList2 countedDlambdaP;
        private double dLambdaProbFunc(int t, int y_val, int k) {
            return dCoefProbFunc(1, countedDlambdaP, t, y_val, k);
        }

        private TDoubleArrayList2 countedDmuP;
        private double dMuProbFunc(int t, int y_val, int k) {
            return dCoefProbFunc(2, countedDmuP, t, y_val, k);
        }

        private TDoubleArrayList2 countedDlambdaFuncSum;
        private double dLambdaFuncSum(int t, int y_val, int k) {
            Attribute attr = observs.getAttribute(t);
            double res = 0d;
            for (int y = 1; y < LABELS_NUMBER; y++) {
                res += crfFunctions.getFFunction(k, y, y_val, attr) * trainingModel.countProbability(t - 1, y);
            }
            return dCoefFuncSum(3, countedDlambdaFuncSum, res, t, y_val, k);
        }

        private TDoubleArrayList2 countedDmuFuncSum;
        private double dMuFuncSum(int t, int y_val, int k) {
            Attribute attr = observs.getAttribute(t);
            double res = crfFunctions.getGFunction(k, y_val, attr);
            return dCoefFuncSum(4, countedDmuFuncSum, res, t, y_val, k);
        }

        private double dCoefFuncSum(int type, TDoubleArrayList2 array, double res, int t, int y_val, int k) {
            if (array.get(t, k, y_val) != -1d) return array.get(t, k, y_val);
            if (t == 0) {
                array.set(t, k, y_val, 0d);
                return 0d;
            }
            Attribute attr = observs.getAttribute(t);
            for (int l = 0; l < fNumber; l++) {
                double dpfSum = 0d;
                for (int y = 1; y < LABELS_NUMBER; y++) {
                    dpfSum += crfFunctions.getFFunction(l, y, y_val, attr) * execute(type,t - 1, y, k);
                }
                res += coefficients.getLambda(l) * dpfSum;
            }
            array.set(t, k, y_val, res);
            return res;
        }

        private double dCoefProbFunc(int type, TDoubleArrayList2 array, int t, int y_val, int k) {
            if (array.get(t, k, y_val) != -1d) return array.get(t, k, y_val);
            if (t == 0) {
                array.set(t, k, y_val, 0d);
                return 0d;
            }
            double res = 0d;
            for (int y = 1; y < LABELS_NUMBER; y++) {
                res += trainingModel.countProbability(t, y) * (execute(type, t, y_val, k) - execute(type, t, y, k));
            }
            res *= trainingModel.countProbability(t, y_val);
            array.set(t, k, y_val, res);
            return res;
        }
    }
}
