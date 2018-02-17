package crf.io;

import crf.utils.Attributes;
import crf.utils.Coefficients;
import crf.utils.Constants;
import crf.utils.Labels;
import gnu.trove.list.array.TIntArrayList;
import org.jdom2.util.IteratorIterable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static crf.utils.Constants.COEFFS_FILE;

public class PrinterCrf {

    public static void printCoeffs(Coefficients coefficients) throws IOException {
        FileWriter fw = new FileWriter(new File(COEFFS_FILE));
        BufferedWriter fout = new BufferedWriter(fw);
        fout.write("___LAMBDAS___\n");
        for (double lambda : coefficients.getAllLambdas()) {
            fout.write(String.valueOf(lambda) + "\n");
        }
        fout.write("___END___\n___MUS___\n");
        for (double mu : coefficients.getAllMus()) {
            fout.write(String.valueOf(mu) + "\n");
        }
        fout.write("___END___");
        fout.close();
        fw.close();
    }

    public static void printTrainingTags(HashSet<String>[][] sortedTags, ArrayList<String> allTags) throws IOException {
        FileWriter fw = new FileWriter(new File(Constants.SORTED_TRAINING_TAGS));
        Labels labels = Labels.getInstance();
        final int defaultLabel = Labels.getInstance().defaultLabel;
        final int labelsSize = Labels.getInstance().getLabelsSize();
        BufferedWriter fout = new BufferedWriter(fw);
        fout.write("___SORTED_TAGS___\n");
        for (int prev_y = defaultLabel; prev_y < labelsSize; prev_y++) {
            for (int y = defaultLabel + 1; y < labelsSize; y++) {
                Iterator<String> iterator = sortedTags[prev_y][y].iterator();
                StringBuilder builder = new StringBuilder();
                builder.append(labels.getLabelByNumber(prev_y)).append("\t")
                        .append(labels.getLabelByNumber(y)).append("\n");
                while (iterator.hasNext()) {
                    builder.append(iterator.next()).append("\n");
                }
                builder.append("___SEQ-END___\n");
                fout.write(builder.toString());
            }
        }
        fout.write("___END___");
        fout.close();
        fw.close();
        fw = new FileWriter(new File(Constants.ALL_TRAINING_TAGS));
        fout = new BufferedWriter(fw);
        fout.write("___ALL_TAGS___\n");
        Iterator<String> iterator = allTags.iterator();
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext()) {
            builder.append(iterator.next()).append("\n");
        }
        builder.append("___END___");
        fout.write(builder.toString());
        fout.close();
        fw.close();
    }

    public static void printLabeling(List<TIntArrayList> result, List<Attributes> attrs) throws IOException {
        FileWriter fw = new FileWriter(new File(Constants.LABELING_RES_FILE));
        BufferedWriter fout = new BufferedWriter(fw);
        Labels labels = Labels.getInstance();
        for (int m = 0; m < result.size(); m++) {
            TIntArrayList hids = result.get(m);
            Attributes observs = attrs.get(m);
            fout.write("\nTEXT NUMBER " + m + ":\n");
            StringBuilder[] s = new StringBuilder[labels.getLabelsSize()];
            for (int i = 0; i < s.length; i++) {
                s[i] = new StringBuilder();
            }
            for (int i = 0; i < hids.size(); i++) {
                int k = hids.get(i);
                if (k != labels.defaultLabel) {
                    s[k] = s[k].append(observs.getAttribute(i).text).append(" ");
                }
            }
            for (int i = 1; i < s.length; i++) {
                if (i != labels.defaultLabel) {
                    fout.write(labels.getLabelByNumber(i) + ":\t" + s[i].toString() + "\n");
                }
            }
        }
        fout.close();
        fw.close();
    }

}
