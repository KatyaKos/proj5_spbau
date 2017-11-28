import java.util.*;

public class CrfImpl {

    protected final int gNumber = 3;
    protected final int fNumber = 3;

    protected ArrayList<Double> mus = new ArrayList<>();
    protected ArrayList<Double> lambdas = new ArrayList<>();

    protected ArrayList<Integer> possibleHiddens = new ArrayList<Integer>() {{add(-1); add(1);}};

    private ArrayList<HashMap<String, String>> observations;

    private HashMap<Integer, ArrayList<Double>> countedProbabilities = new HashMap<>();

    public CrfImpl() {
        mus = new ArrayList<Double>(){{add(1.0267745616383386); add(3.499347641382276); add(1.8528758087861708 );}};
        lambdas = new ArrayList<Double>(){{add(1.337319019901404); add(2.7627438006692215); add(3.401339085262019);}};
        /*for (int i = 0; i < gNumber; i++) {
            mus.add(2d);
        }
        for (int i = 0; i < fNumber; i++) {
            lambdas.add(2d);
        }*/
    }

    private boolean isNumeric(String s) {
        return s.matches("-?\\d+(,\\d+)?(\\.\\d+)?");
        //chars().parallel().anyMatch(ch -> !Character.isDigit(ch) && ch != '.' && ch != ',');
    }

    private boolean tagsContain(String s, String match) {
        String[] tags = s.split("/");
        int n = tags.length;
        for (int i = n - 1; i > n - 6; i--) {
            if (i >= 0 && tags[i].matches(".*:" + match)) return true;
        }
        return false;
    }

    protected Double getGFunction(int type, Integer y, String vals[]) {
        String text = vals[0];
        String path = vals[1];
        switch (type) {
            case 0:
                return isNumeric(text) ? (y == 1 ? 0.25 : 0.75) : (y == 1 ? 0.05 : 0.95);
            case 1:
                return tagsContain(path, "p-price") && tagsContain(path, "p-current-price") ?
                        (y == 1 ? 0.85 : 0.15) : (y == 1 ? 0d : 1d);
            case 2:
                return tagsContain(path, "p-price") && tagsContain(path, "p-current-price") && isNumeric(text) ?
                        (y == 1 ? 0.85 : 0.15) : (y == 1 ? 0.3 : 0.7);
        }
        return 0d;
    }

    protected Double getFFunction(int type, Integer[] hids, String[] vals) {
        Integer prev_y = hids[0];
        Integer y = hids[1];
        String text = vals[0];
        String path = vals[1];
        switch (type) {
            case 0:
                if (isNumeric(text))
                    return prev_y == 1 ? (y == 1 ? 0.4 : 0.6) : (y == 1 ? 0.25 : 0.75);
                else
                    return prev_y == 1 ? (y == 1 ? 0.05 : 0.95) : (y == 1 ? 0d : 1d);
            case 1:
                if (tagsContain(path, "p-price")) {
                    if (tagsContain(path, "p-current-price")) {
                        return prev_y == 1 ? (y == 1 ? 0.7 : 0.3) : (y == 1 ? 0.9 : 0.1);
                    } else return y == 1 ? 0d : 1d;
                } else return y == 1 ? 0d : 1d;
            case 2:
                if (isNumeric(text)) {
                    if (tagsContain(path, "p-price")) {
                        if (tagsContain(path, "p-current-price")) {
                            return prev_y == 1 ? (y == 1 ? 0.95 : 0.05) : (y == 1 ? 0.8 : 0.2);
                        } else return y == 1 ? 0d : 1d;
                    } else return y == 1 ? 0d : 1d;
                } else {
                    if (tagsContain(path, "p-price")) {
                        if (tagsContain(path, "p-current-price")) {
                            return prev_y == 1 ? (y == 1 ? 0.85 : 0.15) : (y == 1 ? 0d : 1d);
                        } else return y == 1 ? 0d : 1d;
                    } else return y == 1 ? 0d : 1d;
                }
        }
        return 0d;
    }

    protected Double countFuncsSum(int pNumber) {
        HashMap<String, String> attrs = observations.get(pNumber);
        String[] obs = new String[] {attrs.get("node"), attrs.get("path")};
        Double fSum = 0d;
        Double prob1 = countProbability(pNumber - 1, 1);
        Double prob0 = countProbability(pNumber - 1, -1);
        for (int j = 0; j < fNumber; j++) {
            fSum += lambdas.get(j) * prob0 *
                    (getFFunction(j, new Integer[]{-1, 1}, obs) - getFFunction(j, new Integer[]{-1, -1}, obs));
            fSum += lambdas.get(j) * prob1 *
                    (getFFunction(j, new Integer[] {1, 1}, obs) - getFFunction(j, new Integer[] {1, -1}, obs));
        }
        Double gSum = 0d;
        for (int j = 0; j < gNumber; j++) gSum += mus.get(j) * (getGFunction(j, 1, obs) - getGFunction(j, -1, obs));
        return fSum + gSum;
    }

    protected Double countProbability(int pNumber, Integer y) {
        Double pr;
        ArrayList<Double> probs = countedProbabilities.get(y);
        if (probs.get(pNumber) != null) return probs.get(pNumber);
        if (pNumber == 0) {
            if (y == 1) pr = 0.001; else pr = 0.999;
            probs.set(pNumber, pr);
            countedProbabilities.put(y, probs);
            return pr;
        }
        pr = 1 / (1 + Math.exp(-y*countFuncsSum(pNumber)));
        probs.set(pNumber, pr);
        countedProbabilities.put(y, probs);
        return pr;
    }

    public ArrayList<ArrayList<Integer>> labelData(ArrayList<ArrayList<HashMap<String, String>>> labeling) {
        ArrayList<ArrayList<Integer>> hids = new ArrayList<>();
        for (int m = 0; m < labeling.size(); m++) {
            ArrayList<HashMap<String, String>> observs = labeling.get(m);
            initializeLabelsData(observs);
            ArrayList<Integer> res_hids = new ArrayList<>();
            for (int i = 0; i < observs.size(); i++) {
                Double p0 = countProbability(i, -1);
                Double p1 = countProbability(i, 1);
                Double summ = p0 + p1;
                if (summ - 0.01 > 1d || summ + 0.01 < 1d) {
                    System.err.println(p0.toString() + "   " + p1.toString());
                }
                if (p0 > 0.5) res_hids.add(-1);
                else res_hids.add(1);
            }
            hids.add(res_hids);
        }
        return hids;
    }

    public void initializeLabelsData(ArrayList<HashMap<String, String>> observations) {
        this.observations = observations;
        countedProbabilities.clear();
        for (int i = -1; i < possibleHiddens.size(); i += 2) {
            countedProbabilities.put(i, new ArrayList<>(Collections.nCopies(observations.size(), null)));
        }
    }
}
