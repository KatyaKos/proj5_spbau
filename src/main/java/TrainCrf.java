import com.expleague.commons.text.StringUtils;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import javafx.util.Pair;

import java.util.*;

//Gradient Descent
public class TrainCrf extends CrfImpl {

    private Double alpha = 0.01;

    private int steps = 100;

    private List<TIntArrayList> hiddens;
    private List<List<Map<String, String>>> observations;
    private TIntArrayList hids = new TIntArrayList();
    private List<Map<String, String>> observs = new ArrayList<>();

    private TDoubleArrayList dfTails = new TDoubleArrayList();

    public TrainCrf(List<TIntArrayList> hiddens, List<List<Map<String, String>>> observations) {
        this.hiddens = hiddens;
        this.observations = observations;
        fillTrainingTags();
        gNumber = trainingTags.length + 1;
        fNumber = trainingTags.length + 1;
        for (int i = 0; i < gNumber; i++) {
            mus.add(1d);
        }
        for (int i = 0; i < fNumber; i++) {
            lambdas.add(1d);
        }
    }

    private void fillTrainingTags() {
        HashSet<String> result = new HashSet<>();
        for (int m = 0; m < hiddens.size(); m++) {
            hids = hiddens.get(m);
            observs = observations.get(m);
            for (int t = 0; t < hids.size(); t++) {
                if (hids.get(t) == 1) {
                    List<String> tags = StringUtils.split2List(observs.get(t).get("path"),"/");
                    int n = tags.size();
                    result.addAll(tags.subList(n - TAGS_NUMBER, n));
                }
            }
        }
        trainingTags = result.toArray(new String[result.size()]);
    }

    private Double countDfTail(int k, int t) {
        if (dfTails.get(t) != 0d) return dfTails.get(t);
        String[] attrs = new String[] {observs.get(t).get("node"), observs.get(t).get("path")};
        Double deltaF0 = getFFunction(k, new int[]{-1, 1}, attrs) - getFFunction(k, new int[]{-1, -1}, attrs);
        Double deltaF1 = getFFunction(k, new int[]{1, 1}, attrs) - getFFunction(k, new int[]{1, -1}, attrs);
        //Double pfSum = countProbability(t, -1) * deltaF0 + countProbability(t, 1) * deltaF1;
        Double res = countProbability(t, -1) * deltaF0 + countProbability(t, 1) * deltaF1;
        if (t != 0) {
            Double power = countFuncsSum(t);
            Double lambdaTail = Math.exp(-power) * Math.pow(countProbability(t, 1), 2) * deltaF1 -
                    Math.exp(power) * Math.pow(countProbability(t, -1), 2) * deltaF0;
            res += lambdas.get(k) * lambdaTail * countDfTail(k, t - 1);
        }
        dfTails.set(t, res);
        return res;
    }

    private ArrayList<Double> dlambdaFunc() {
        ArrayList<Double> dl = new ArrayList<>();
        for (int k = 0; k < lambdas.size(); k++){
            Double res = 0d;
            for (int m = 0; m < hiddens.size(); m++) {
                hids = hiddens.get(m);
                observs = observations.get(m);
                dfTails = fillTDouble(observs.size(), 0d);
                initializeLabelsData(observs);
                for (int t = 1; t < hids.size(); t++) {
                    if (hids.get(t) == 1) res += countProbability(t, -1) * countDfTail(k, t - 1);
                    else res += -countProbability(t, 1) * countDfTail(k, t - 1);
                }
            }
            dl.add(res);
        }
        return dl;
    }

    private ArrayList<Double> dmuFunc() {
        ArrayList<Double> dmu = new ArrayList<>();
        for (int k = 0; k < lambdas.size(); k++){
            Double res = 0d;
            for (int m = 0; m < hiddens.size(); m++) {
                hids = hiddens.get(m);
                observs = observations.get(m);
                initializeLabelsData(observs);
                for (int t = 1; t < hids.size(); t++) {
                    String[] attrs = new String[] {observs.get(t).get("node"), observs.get(t).get("path")};
                    Double deltaG = getGFunction(k, 1, attrs) - getGFunction(k, -1, attrs);
                    if (hids.get(t) == 1) res += countProbability(t, -1) * deltaG;
                    else res += -countProbability(t, 1) * deltaG;
                }
            }
            dmu.add(res);
        }
        return dmu;
    }

    private void move() {
        ArrayList<Double> dlambda = dlambdaFunc();
        ArrayList<Double> dmu = dmuFunc();
        for (int i = 0; i < lambdas.size(); i++)
            lambdas.set(i, lambdas.get(i) + alpha * dlambda.get(i));
        for (int i = 0; i < mus.size(); i++)
            mus.set(i, mus.get(i) + alpha * dmu.get(i));
    }

    public Pair<TDoubleArrayList, TDoubleArrayList> train() {
        int say = steps / 10;
        System.out.printf("TOTAL STEPS:%d\n", steps);
        for (int step = 0; step < steps; step++) {
            move();
            if (step % say == 0) System.out.printf("STEP NUMBER:%d\n", step);
        }
        return new Pair<>(lambdas, mus);
    }
}