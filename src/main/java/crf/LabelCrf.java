package crf;

import crf.utils.Attributes;
import crf.utils.Coefficients;
import crf.utils.Constants;
import crf.utils.Labels;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LabelCrf extends CrfImpl {

    private final int LABELS_NUMBER = Labels.getInstance().getLabelsSize();

    public LabelCrf(Coefficients coefficients, HashSet<String>[][] sortedTags, ArrayList<String> allTags){
        this.coefficients = coefficients;
        final int tags = allTags.size();
        this.gNumber = tags + Constants.EXTRA_G_FUNCTIONS;
        this.fNumber = tags + Constants.EXTRA_F_FUNCTIONS;
        this.crfFunctions = CrfFunctions.getInstance(sortedTags, allTags);
    }

    public LabelCrf(TrainCrf trainedModel) {
        this.coefficients = trainedModel.coefficients;
        this.gNumber = trainedModel.gNumber;
        this.fNumber = trainedModel.fNumber;
        this.crfFunctions = CrfFunctions.getInstance();
    }

    public List<TIntArrayList> labelData(List<Attributes> attributes) {
        List<TIntArrayList> hids = new ArrayList<>();
        for (Attributes observs : attributes) {
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
                //if (p[2] >= p[1]) max_k = 2;
                res_hids.add(max_k);
            }
            hids.add(res_hids);
        }
        return hids;
    }
}
