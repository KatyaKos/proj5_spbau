package crf;

import crf.utils.Attributes;
import crf.utils.Coefficients;
import crf.utils.Constants;
import crf.utils.Labels;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class LabelCrf extends CrfImpl {

    private final int LABELS_NUMBER = Labels.getInstance().getLabelsSize();

    public LabelCrf(Coefficients coefficients, HashSet<String>[] sortedTags, ArrayList<String> allTags){
        this.coefficients = coefficients;
        final int tags = allTags.size();
        this.gNumber = tags + Constants.EXTRA_G_FUNCTIONS;
        this.fNumber = tags + Constants.EXTRA_F_FUNCTIONS;
        this.crfFunctions = CrfFunctions.getInstance(sortedTags, allTags);
    }

    public LabelCrf(TrainCrf trainedModel) {
        /*this.countedSums = trainedModel.countedSums;
        this.countedExps = trainedModel.countedExps;
        this.countedExpSums = trainedModel.countedExpSums;
        this.countedProbabilities = trainedModel.countedProbabilities;*/
        this.coefficients = trainedModel.coefficients;
        this.gNumber = trainedModel.gNumber;
        this.fNumber = trainedModel.fNumber;
        this.crfFunctions = CrfFunctions.getInstance();
    }

    public List<TIntArrayList> labelData(List<Attributes> attributes) {
        List<TIntArrayList> hids = new ArrayList<>();
        for (int m = 0; m < attributes.size(); m++) {
            Attributes observs = attributes.get(m);
            initializeLabelsData(observs);
            TIntArrayList res_hids = new TIntArrayList();
            for (int i = 0; i < observs.size(); i++) {
                double[] p = new double[LABELS_NUMBER];
                int max_k = 0;
                double max_p = -1d;
                for (int k = 1; k < LABELS_NUMBER; k++) {
                    p[k] = countProbability(i, k);
                    if (p[k] > max_p) {
                        max_p = p[k];
                        max_k = k;
                    }
                }
                // TODO эту фигню и массив р удалить
                if (observs.getAttribute(i).text.equals("350")) {
                    double sdf = Arrays.stream(p).sum();
                }
                double summ = Arrays.stream(p).sum();
                if (summ - 0.01 > 1d || summ + 0.01 < 1d) {
                    System.err.println("Text " + m + " line " + i);
                }
                // TODO заменить на что-то нормальное
                if (p[2] >= p[1]) max_k = 2;
                res_hids.add(max_k);
            }
            hids.add(res_hids);
        }
        return hids;
    }
}
