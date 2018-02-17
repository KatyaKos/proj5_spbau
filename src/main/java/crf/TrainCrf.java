package crf;

import com.expleague.commons.text.StringUtils;
import crf.utils.*;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

import static crf.utils.Constants.GRAD_DESC_STEPS;

//Gradient Descent
public class TrainCrf extends CrfImpl {

    private List<TIntArrayList> hiddens;
    private List<Attributes> observations;

    public TrainCrf(List<TIntArrayList> hiddens, List<Attributes> observations) {
        this.hiddens = hiddens;
        this.observations = observations;
        final int tags = fillTrainingTags();
        gNumber = tags + Constants.EXTRA_G_FUNCTIONS;
        fNumber = tags + Constants.EXTRA_F_FUNCTIONS;
        crfFunctions = CrfFunctions.getInstance();
        setCoefficients(null);
    }

    public void setCoefficients(Coefficients coefs) {
        if (coefs != null) {
            coefficients = coefs;
            return;
        }
        if (coefficients != null) return;
        coefficients = new Coefficients(fNumber, gNumber);
        for (int i = 0; i < gNumber; i++) {
            coefficients.setMu(i, 1d);
        }
        for (int i = 0; i < fNumber; i++) {
            coefficients.setLambda(i, 1d);
        }
    }

    private int fillTrainingTags() {
        final int labs = Labels.getInstance().getLabelsSize();
        final int defaultLabel = Labels.getInstance().defaultLabel;
        final int tagsNumber = Constants.TAGS_NUMBER;
        HashSet<String>[][] sortedTags = new HashSet[labs][labs];
        for (int i = 0; i < sortedTags.length; i++)
            for (int j = 0; j < sortedTags.length; j++) sortedTags[i][j] = new HashSet<>();
        HashSet<String> allTags = new HashSet<>();

        for (int m = 0; m < hiddens.size(); m++) {
            TIntArrayList hids = hiddens.get(m);
            Attributes observs = observations.get(m);
            int prev_y = defaultLabel;
            for (int t = 1; t < hids.size(); t++) {
                int y = hids.get(t);
                if (y != defaultLabel) {
                    List<String> tags = StringUtils.split2List(observs.getAttribute(t).path,"/");
                    final int n = tags.size();
                    tags = tags.subList(Math.max(0, n - tagsNumber), n - 1);
                    sortedTags[prev_y][y].addAll(tags);
                    allTags.addAll(tags);
                }
                prev_y = y;
            }
        }
        CrfFunctions.getInstance(sortedTags, new ArrayList<>(allTags));
        return allTags.size();
    }

    public Coefficients train() {
        int say = GRAD_DESC_STEPS / 10;
        if (say == 0) say = 1;
        GradDescent gradDescent = new GradDescent(this, hiddens, observations);
        System.out.printf("TOTAL STEPS:%d\n", GRAD_DESC_STEPS);
        for (int step = 0; step < GRAD_DESC_STEPS; step++) {
            gradDescent.move();
            if (step % say == 0) {
                System.out.printf("STEP NUMBER:%d\n", step);
            }
        }
        return coefficients;
    }
}