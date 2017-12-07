import com.expleague.commons.seq.CharSeqTools;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.*;
import java.util.regex.Pattern;

public class CrfImpl {

    public static final int[] HIDS = {-1, 1};
    public static final int[] HIDS1 = {-1, -1};
    public static final int[] HIDS2 = {1, 1};
    public static final int[] HIDS3 = {1, -1};
    protected int gNumber;
    protected int fNumber;

    protected final int TAGS_NUMBER = 6;
    private final Pattern pattern = Pattern.compile("-?\\d+(,\\d+)?(\\.\\d+)?");
    protected String[] trainingTags;

    protected TDoubleArrayList mus = new TDoubleArrayList();
    protected TDoubleArrayList lambdas = new TDoubleArrayList();

    protected TIntArrayList possibleHiddens = new TIntArrayList(new int[]{-1, 1});

    private List<Map<String, String>> observations;

    private TIntObjectMap<TDoubleArrayList> countedProbabilities = new TIntObjectHashMap<>();

    public CrfImpl() {
        //mus = new TDoubleArrayList(new double[]{1.0267745616383386, 3.499347641382276, 1.8528758087861708});
        //lambdas = new TDoubleArrayList(new double[]{1.337319019901404, 2.7627438006692215, 3.401339085262019});

        /*for (int i = 0; i < gNumber; i++) {
            mus.add(1d);
        }
        for (int i = 0; i < fNumber; i++) {
            lambdas.add(1d);
        }*/
    }

    static long counter = 0;
    TObjectIntMap<String> numericCache = new TObjectIntHashMap<>();
    boolean isNumeric(String s) {
        //return pattern.matcher(s).matches();
        int cached = numericCache.get(s);
        if (cached != numericCache.getNoEntryValue())
            return cached > 0;
        boolean result = s.chars().parallel().noneMatch(x -> x != ',' && x != '.' && Character.isDigit(x));
        numericCache.put(s, result ? 1 : -1);
        return result;
//
//        char[] chars = s.toCharArray();
//        if (chars.length == 0) return false;
//        int i = 0;
//        int n = chars.length;
//        if (chars[0] == '-' && n > 1  || Character.isDigit(chars[0])) i++;
//        else return false;
//        while (true) {
//            while (i < n && Character.isDigit(chars[i])) i++;
//            if (i == n) return true;
//            if (chars[i] == ',' && i < n - 1) i++;
//            else if (chars[i] == '.') break;
//            else return false;
//        }
//        i++;
//        if (i == n) return false;
//        while (i < n && Character.isDigit(chars[i])) i++;
//        return i == n;
    }

    Map<String, TObjectIntMap<String>> cache = new HashMap<>();
    private boolean isTag(String s, String match) {
        TObjectIntMap<String> known = cache.computeIfAbsent(match, k -> new TObjectIntHashMap<>());
        int result = known.get(s);
        if (result != known.getNoEntryValue())
            return result > 0;
        CharSequence[] tags = CharSeqTools.split(s, "/");
        int n = tags.length;
        for (int i = n - TAGS_NUMBER; i < n; i++) {
            if (i >= 0 && tags[i].equals(match)) {
                result = 1;
                break;
            }
        }
        if (result == 0)
            result = -1;
        known.put(s, result);
        return result > 0;
    }

    protected double getGFunction(int type, int y, String vals[]) {
        String text = vals[0];
        String path = vals[1];
        if (type == 0) return isNumeric(text) ? (y == 1 ? 0.25 : 0.75) : (y == 1 ? 0.05 : 0.95);
        if (type <= trainingTags.length)
            return isTag(path, trainingTags[type - 1]) ? (y == 1 ? 0.8 : 0.2) : (y == 1 ? 0.3 : 0.7);
        return 0d;
    }

    protected double getFFunction(int type, int[] hids, String[] vals) {
        int prev_y = hids[0];
        int y = hids[1];
        String text = vals[0];
        String path = vals[1];
        if (type == 0) {
            if (isNumeric(text))
                return prev_y == 1 ? (y == 1 ? 0.4 : 0.6) : (y == 1 ? 0.25 : 0.75);
            else
                return prev_y == 1 ? (y == 1 ? 0.05 : 0.95) : (y == 1 ? 0d : 1d);
        }
        if (type <= trainingTags.length) {
            if (isTag(path, trainingTags[type - 1])) {
                return prev_y == 1 ? (y == 1 ? 0.4 : 0.6) : (y == 1 ? 0.85 : 0.15);
            } else {
                return prev_y == 1 ? (y == 1 ? 0.1 : 0.9) : (y == 1 ? 0.3 : 0.7);
            }
        }
        return 0d;
    }

    protected double countFuncsSum(int pNumber) {
        Map<String, String> attrs = observations.get(pNumber);
        String[] obs = new String[] {attrs.get("node"), attrs.get("path")};
        double fSum = 0d;
        double prob1 = countProbability(pNumber - 1, 1);
        double prob0 = countProbability(pNumber - 1, -1);
        for (int j = 0; j < fNumber; j++) {
            fSum += lambdas.get(j) * prob0 *
                    (getFFunction(j, HIDS, obs) - getFFunction(j, HIDS1, obs));
            fSum += lambdas.get(j) * prob1 *
                    (getFFunction(j, HIDS2, obs) - getFFunction(j, HIDS3, obs));
        }
        double gSum = 0d;
        for (int j = 0; j < gNumber; j++) gSum += mus.get(j) * (getGFunction(j, 1, obs) - getGFunction(j, -1, obs));
        return fSum + gSum;
    }

    protected double countProbability(int pNumber, int y) {
        double pr;
        TDoubleArrayList probs = countedProbabilities.get(y);
        if (probs.get(pNumber) != -1d) return probs.get(pNumber);
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

    public List<TIntArrayList> labelData(List<List<Map<String, String>>> labeling) {
        List<TIntArrayList> hids = new ArrayList<>();
        for (int m = 0; m < labeling.size(); m++) {
            List<Map<String, String>> observs = labeling.get(m);
            initializeLabelsData(observs);
            TIntArrayList res_hids = new TIntArrayList();
            for (int i = 0; i < observs.size(); i++) {
                double p0 = countProbability(i, -1);
                double p1 = countProbability(i, 1);
                double summ = p0 + p1;
                if (summ - 0.01 > 1d || summ + 0.01 < 1d) {
                    System.err.println(p0 + "   " + p1);
                }
                if (p0 > 0.5) res_hids.add(-1);
                else res_hids.add(1);
            }
            hids.add(res_hids);
        }
        return hids;
    }

    public void initializeLabelsData(List<Map<String, String>> observations) {
        this.observations = observations;
        countedProbabilities.clear();
        TDoubleArrayList temp = fillTDouble(observations.size(), -1d);
        for (int i = -1; i < possibleHiddens.size(); i += 2) {
            countedProbabilities.put(i, new TDoubleArrayList(temp));
        }
    }

    protected TDoubleArrayList fillTDouble(int size, double value) {
        double[] tmp = new double[size];
        for (int i = 0; i < size; i++) tmp[i] = value;
        return new TDoubleArrayList(tmp);
    }
}