package crf.io;

import com.expleague.commons.seq.CharSeqTools;
import com.expleague.commons.text.StringUtils;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import crf.utils.Attributes;
import crf.utils.Coefficients;
import crf.utils.Constants;
import crf.utils.Labels;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static crf.utils.Constants.patternAttr;

public class ReaderCrf {
    public static void readAllLabels() throws IOException {
        File file = new File(Constants.LABELS_FILE);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        List<String> labels = new ArrayList<>();
        String s;
        while ((s = fin.readLine()) != null) {
            labels.add(s);
        }
        Labels.getInstance().init(labels);
    }

    public static List<TIntArrayList> readLabels(String fileName) throws IOException {
        readAllLabels();
        Labels labels = Labels.getInstance();
        List<TIntArrayList> result = new ArrayList<>();
        File file = new File(fileName);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        while (fin.readLine() != null) {
            String s;
            TIntArrayList hids = new TIntArrayList();
            hids.add(labels.defaultLabel);
            while (!(s = fin.readLine()).equals("___END___")) {
                String[] content = StringUtils.split(s, "\t", 0);
                int y = labels.getNumberByLabel(content[0]);
                assert y != 0;
                hids.add(labels.getNumberByLabel(content[0]));
            }
            hids.add(labels.defaultLabel);
            result.add(hids);
        }
        return result;
    }

    public static List<Attributes> readAttributes(String fileName) throws IOException {
        List<Attributes> result = new ArrayList<>();
        File file = new File(fileName);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        while (fin.readLine() != null) {
            String s;
            Attributes observs = new Attributes();
            observs.addAttribute("", "<html>");
            while (!(s = fin.readLine()).equals("___END___")) {
                CharSequence[] content = CharSeqTools.split(s, "\t");
                observs.addAttribute(patternAttr.split(content[1])[1], patternAttr.split(content[2])[1]);
            }
            observs.addAttribute("", "<html>");
            result.add(observs);
        }
        return result;
    }

    public static Coefficients readCoefficients() throws IOException {
        File file = new File(Constants.COEFFS_FILE);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        String s;
        int lNumber = 0;
        TDoubleArrayList lambdas = new TDoubleArrayList();
        fin.readLine();
        while (!(s = fin.readLine()).equals("___END___")) {
            lNumber++;
            lambdas.add(Double.parseDouble(s));
        }
        fin.readLine();
        int mNumber = 0;
        TDoubleArrayList mus = new TDoubleArrayList();
        while (!(s = fin.readLine()).equals("___END___")) {
            mNumber++;
            mus.add(Double.parseDouble(s));
        }
        Coefficients coefficients = new Coefficients(lNumber, mNumber);
        for (int i = 0; i < lNumber; i++) coefficients.setLambda(i, lambdas.get(i));
        for (int i = 0; i < mNumber; i++) coefficients.setMu(i, mus.get(i));
        return coefficients;
    }


    public static HashSet<String>[][] readSortedTags() throws IOException {
        File file = new File(Constants.SORTED_TRAINING_TAGS);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        String s;
        final Labels labels = Labels.getInstance();
        final int labelsSize = labels.getLabelsSize();
        HashSet<String>[][] result = new HashSet[labelsSize][labelsSize];
        for (int i = 0; i < labelsSize; i++)
            for (int j = 0; j < labelsSize; j++) result[i][j] = new HashSet<>();
        fin.readLine();
        while (!(s = fin.readLine()).equals("___END___")) {
            final List<String> labs = StringUtils.split2List(s, "\t");
            assert labs.size() == 2;
            final int prev_y = labels.getNumberByLabel(labs.get(0));
            final int y = labels.getNumberByLabel(labs.get(1));
            while (!(s = fin.readLine()).equals("___SEQ-END___")) {
                result[prev_y][y].add(s);
            }
        }
        return result;
    }
    public static ArrayList<String> readAllTags() throws IOException {
        File file = new File(Constants.ALL_TRAINING_TAGS);
        BufferedReader fin = new BufferedReader(new FileReader(file));
        String s;
        ArrayList<String> result = new ArrayList<>();
        fin.readLine();
        while (!(s = fin.readLine()).equals("___END___")) {
            result.add(s);
        }
        return result;
    }
}
