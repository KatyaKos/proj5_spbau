package crf;

import crf.utils.Attributes;
import crf.utils.Coefficients;
import crf.utils.Labels;
import gnu.trove.list.array.TDoubleArrayList;

import static crf.utils.TDoubleArrayList2.fillTDouble;

public abstract class CrfImpl {

    public int gNumber;
    public int fNumber;

    public Coefficients coefficients;
    private Attributes observations;
    private final Labels labels = Labels.getInstance();
    private final int LABELS_NUMBER = Labels.getInstance().getLabelsSize();
    CrfFunctions crfFunctions;

    private TDoubleArrayList[] countedSums;
    private double countFuncSum(int pNumber, int y) {
        if (countedSums[pNumber].get(y) != 0d) return countedSums[pNumber].get(y);
        Attributes.Attribute attrs = observations.getAttribute(pNumber);
        double fSum = 0d;
        if (pNumber > 0) {
            double[] probs = new double[LABELS_NUMBER];
            for (int i = 1; i < LABELS_NUMBER; i++)
                probs[i] = countProbability(pNumber - 1, i);
            for (int j = 0; j < fNumber; j++) {
                double l = coefficients.getLambda(j);
                for (int i = 1; i < LABELS_NUMBER; i++) {
                    fSum += l * probs[i] * crfFunctions.getFFunction(j, i, y, attrs);
                }
            }
        }
        double gSum = 0d;
        for (int j = 0; j < gNumber; j++) gSum += coefficients.getMu(j) * crfFunctions.getGFunction(j, y, attrs);
        countedSums[pNumber].set(y, fSum + gSum);
        return fSum + gSum;
    }


    private TDoubleArrayList[] countedExps;
    private double countExp(int pNumber, int y) {
        if (countedExps[pNumber].get(y) != 0d) return countedExps[pNumber].get(y);
        final double res = Math.exp(countFuncSum(pNumber, y));
        countedExps[pNumber].set(y, res);
        return res;
    }

    private TDoubleArrayList countedExpSums;
    private double countExpSum(int pNumber) {
        if (countedExpSums.get(pNumber) != 0d) return countedExpSums.get(pNumber);
        double res = 0d;
        for (int i = 1; i < LABELS_NUMBER; i++) {
            res += countExp(pNumber, i);
        }
        countedExpSums.set(pNumber, res);
        return res;
    }

    private TDoubleArrayList[] countedProbabilities;
    public double countProbability(int pNumber, int y) {
        double pr;
        TDoubleArrayList probs = countedProbabilities[y];
        if (probs.get(pNumber) != -1d) return probs.get(pNumber);
        if (pNumber == 0) {
            if (y == labels.getNumberByLabel("")) pr = 0.9;
            else pr = 0.1 / (LABELS_NUMBER - 2);
        } else {
            pr = countExp(pNumber, y) / countExpSum(pNumber);
        }
        probs.set(pNumber, pr);
        countedProbabilities[y] = probs;
        return pr;
    }

    public void initializeLabelsData(Attributes observations) {
        this.observations = observations;
        countedProbabilities = new TDoubleArrayList[LABELS_NUMBER];
        countedProbabilities[0] = null; // нулевого лейбла нет
        TDoubleArrayList temp = fillTDouble(observations.size(), -1d);
        for (int i = 1; i < LABELS_NUMBER; i++) {
            countedProbabilities[i] = new TDoubleArrayList(temp);
        }
        countedExps = new TDoubleArrayList[observations.size()];
        temp = fillTDouble(LABELS_NUMBER, 0d);
        for (int i = 0; i < observations.size(); i++) {
            countedExps[i] = new TDoubleArrayList(temp);
        }
        countedSums = new TDoubleArrayList[observations.size()];
        for (int i = 0; i < observations.size(); i++) {
            countedSums[i] = new TDoubleArrayList(temp);
        }
        countedExpSums = fillTDouble(observations.size(), 0d);
    }
}