import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

//Gradient Descent
public class TrainCrf extends CrfImpl {

    private Double alpha = 0.005;

    private int steps = 1;

    private ArrayList<ArrayList<Integer>> hiddens;
    private ArrayList<ArrayList<HashMap<String, String>>> observations;

    public TrainCrf(ArrayList<ArrayList<Integer>> hiddens, ArrayList<ArrayList<HashMap<String, String>>> observations) {
        super();
        this.hiddens = hiddens;
        this.observations = observations;
    }

    private Double countFfuncMultP(int k, Integer hid, String obs1, String obs2, int i) {
        return getFFunction(k, new Integer[] {0, hid}, new String[] {obs1, obs2}) * countNthProbability(i, 0) +
                getFFunction(k, new Integer[] {1, hid}, new String[] {obs1, obs2}) * countNthProbability(i,1);
    }

    private ArrayList<Double> dlambdaFunc() {
        ArrayList<Double> dl = new ArrayList<>();
        for (int k = 0; k < lambdas.size(); k++){
            Double res = 0d;
            for (int m = 0; m < hiddens.size(); m++) {
                ArrayList<Integer> hids = hiddens.get(m);
                ArrayList<HashMap<String, String>> observs = observations.get(m);
                initializeLabelsData(observs);
                ArrayList<Double> probabilities = new ArrayList<>();
                for (int i = 0; i < hids.size(); i++) probabilities.add(countNthProbability(i, hids.get(i)));
                Double sumEfp = 0d;
                Double sumFp = 0d;
                for (int i = 1; i < hids.size(); i++) {
                    Double fp = countFfuncMultP(k, hids.get(i), observs.get(i).get("node"), observs.get(i).get("path"), i - 1);
                    sumEfp += fp * probabilities.get(i);
                    sumFp += fp;
                }
                res += sumEfp / probabilities.stream().reduce(0d, (a, b) -> a + b) - sumFp;
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
                ArrayList<Integer> hids = hiddens.get(m);
                ArrayList<HashMap<String, String>> observs = observations.get(m);
                initializeLabelsData(observs);
                ArrayList<Double> probabilities = new ArrayList<>();
                for (int i = 0; i < hids.size(); i++) probabilities.add(countNthProbability(i, hids.get(i)));
                Double sumEg = 0d;
                Double sumG = 0d;
                for (int i = 1; i < hids.size(); i++) {
                    Double g = getGFunction(k, hids.get(i),
                            new String[] {observs.get(i).get("node"), observs.get(i).get("path")});
                    sumEg += g * probabilities.get(i);
                    sumG += g;
                }
                res += sumEg / probabilities.stream().reduce(0d, (a, b) -> a + b) - sumG;
            }
            dmu.add(res);
        }
        return dmu;
    }

    private void move() {
        ArrayList<Double> dlambda = dlambdaFunc();
        ArrayList<Double> dmu = dmuFunc();
        for (int i = 0; i < lambdas.size(); i++)
            lambdas.set(i, lambdas.get(i) - alpha * dlambda.get(i));
        for (int i = 0; i < mus.size(); i++)
            mus.set(i, mus.get(i) - alpha * dmu.get(i));
    }

    public Pair<ArrayList<Double>, ArrayList<Double>> train() {
        for (int step = 0; step < steps; step++) {
            //System.out.println("step=" + step);
            move();
        }
        return new Pair<>(lambdas, mus);
    }
}
