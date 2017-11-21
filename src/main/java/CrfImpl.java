import java.util.*;

public class CrfImpl {

    protected final int gNumber = 3;
    protected final int fNumber = 4;

    protected ArrayList<Double> mus = new ArrayList<>();
    protected ArrayList<Double> lambdas = new ArrayList<>();

    private ArrayList<HashMap<String, String>> observations;

    private HashMap<Integer, ArrayList<Double>> countedProbabilities = new HashMap<>();

    public CrfImpl() {
        for (int i = 0; i < gNumber; i++) {
            mus.add(0.01);
        }
        for (int i = 0; i < fNumber; i++) {
            lambdas.add(0.001);
        }
    }

    private boolean isNumeric(String s) {
        return s.matches("\\d+(\\.|,\\d+)?");
    }

    private boolean isLastPrice(String s) {
        String[] tags = s.split(">");
        return tags[tags.length - 1].contains("price");
    }

    protected Double getGFunction(int type, Integer hid, String vals[]) {
        switch (type) {
            case 0:
                return isNumeric(vals[0]) ? (hid == 1 ? 0.75 : 0.25) : (hid == 1 ? 0d : 1d);
            case 1:
                return isLastPrice(vals[0]) ? (hid == 1 ? 0.75 : 0.25) : (hid == 1 ? 0d : 1d);
            case 2:
                if (isNumeric(vals[0])) {
                    return isLastPrice(vals[0]) ? (hid == 1 ? 0.9 : 0.1) : (hid == 1 ? 0d : 1d);
                } else {
                    return hid == 1 ? 0d : 1d;
                }
        }
        return -1d;
    }

    protected Double getFFunction(int type, Integer[] hids, String[] vals) {
        if (hids[0] == 1)
            return hids[1] == 1 ? 0d : 1d;
        Integer hid = hids[1];
        switch (type) {
            case 0:
                return hids[1] == 1 ? 0.005 : 0.095;
            case 1:
                return isNumeric(vals[0]) ? (hid == 1 ? 0.8 : 0.2) : (hid == 1 ? 0d : 1d);
            case 2:
                return isLastPrice(vals[0]) ? (hid == 1 ? 0.8 : 0.2) : (hid == 1 ? 0d : 1d);
            case 3:
                if (isNumeric(vals[0])) {
                    return isLastPrice(vals[0]) ? (hid == 1 ? 0.95 : 0.05) : (hid == 1 ? 0d : 1d);
                } else {
                    return hid == 1 ? 0d : 1d;
                }
        }
        return -1d;
    }

    private Double countExpPower(int pNumber, Integer y) {
        HashMap<String, String> attrs = observations.get(pNumber);
        String[] obs = new String[] {attrs.get("node"), attrs.get("path")};
        Double fSum = 0d;
        Double prob1 = countNthProbability(pNumber - 1, 1);
        Double prob0 = countNthProbability(pNumber - 1, 0);
        for (int j = 0; j < fNumber; j++) {
            fSum += lambdas.get(j) * prob0 *
                    getFFunction(j, new Integer[]{0, y}, obs);
            fSum += lambdas.get(j) * prob1 *
                    getFFunction(j, new Integer[] {1, y}, obs);
        }
        Double gSum = 0d;
        for (int j = 0; j < gNumber; j++) gSum += mus.get(j) * getGFunction(j, y, obs);
        return fSum + gSum;
    }

    public Double countNthProbability(int pNumber, Integer y) {
        Double pr;
        ArrayList<Double> probs = countedProbabilities.get(y);
        if (probs.get(pNumber) != null) return probs.get(pNumber);
        if (pNumber == 0) {
            if (y == 1) pr = 0.001; else pr = 0.999;
            probs.set(pNumber, pr);
            countedProbabilities.put(y, probs);
            return pr;
        }
        pr = Math.exp(countExpPower(pNumber, y));
        if (pr.isNaN()) {
            System.out.println("hi");
        }
        probs.set(pNumber, pr);
        countedProbabilities.put(y, probs);
        return pr;
    }

    public Double countWholeProbability(ArrayList<Integer> hiddens, ArrayList<HashMap<String, String>> observations) {
        initializeLabelsData(observations);
        return 0d;
        //countNthProbability(hiddens.size(), o)
        //return probabilities.stream().reduce(1d, (a, b) -> a * b) /
         //       probabilities.stream().reduce(0d, (a, b) -> a + b);
    }

    public void initializeLabelsData(ArrayList<HashMap<String, String>> observations) {
        this.observations = observations;
        countedProbabilities.clear();
        countedProbabilities.put(0, new ArrayList<>(Collections.nCopies(observations.size(), null)));
        countedProbabilities.put(1, new ArrayList<>(Collections.nCopies(observations.size(), null)));
    }
}
