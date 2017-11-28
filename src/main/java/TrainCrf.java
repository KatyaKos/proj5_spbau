import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//Gradient Descent
public class TrainCrf extends CrfImpl {

    private Double alpha = 0.01;

    private int steps = 100;

    private ArrayList<ArrayList<Integer>> hiddens;
    private ArrayList<ArrayList<HashMap<String, String>>> observations;
    private ArrayList<Integer> hids = new ArrayList<>();
    private ArrayList<HashMap<String, String>> observs = new ArrayList<>();

    private ArrayList<Double> dfTails = new ArrayList<>();

    public TrainCrf(ArrayList<ArrayList<Integer>> hiddens, ArrayList<ArrayList<HashMap<String, String>>> observations) {
        super();
        this.hiddens = hiddens;
        this.observations = observations;
    }

    private Double countDfTail(int k, int t) {
        if (dfTails.get(t) != null) return dfTails.get(t);
        String[] attrs = new String[] {observs.get(t).get("node"), observs.get(t).get("path")};
        Double deltaF0 = getFFunction(k, new Integer[]{-1, 1}, attrs) - getFFunction(k, new Integer[]{-1, -1}, attrs);
        Double deltaF1 = getFFunction(k, new Integer[]{1, 1}, attrs) - getFFunction(k, new Integer[]{1, -1}, attrs);
        Double pfSum = countProbability(t, -1) * deltaF0 + countProbability(t, 1) * deltaF1;
        Double res = pfSum;
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
                dfTails = new ArrayList<>(Collections.nCopies(observs.size(), null));
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

    public Pair<ArrayList<Double>, ArrayList<Double>> train() {
        for (int step = 0; step < steps; step++) {
            //System.out.println("step=" + step);
            move();
        }
        return new Pair<>(lambdas, mus);
    }
}
