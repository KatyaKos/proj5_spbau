import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class CrfMain {

    private static ArrayList<Double> lambdas;
    private static ArrayList<Double> mus;

    private static ArrayList<ArrayList<Integer>> hiddenValues = new ArrayList<>();
    private static ArrayList<ArrayList<HashMap<String, String>>> observations = new ArrayList<>();

    private static BufferedReader fin;

    public static void main(String[] args) throws IOException {
        Scanner inScanner = new Scanner(System.in);
        File file = new File(inScanner.nextLine());
        fin = new BufferedReader(new FileReader(file));
        readTrainingDataFile();
        TrainCrf trainCrf = new TrainCrf(hiddenValues, observations);
        Pair<ArrayList<Double>, ArrayList<Double>> res = trainCrf.train();
        lambdas = res.getKey();
        mus = res.getValue();
        printAnswer();
    }

    private static void printAnswer() {
        System.out.print("Lambdas:  ");
        for (Double lambda : lambdas) {
            System.out.print(lambda);
            System.out.print("  ");
        }
        System.out.println();
        System.out.print("Mus:  ");
        for (Double mu : mus) {
            System.out.print(mu);
            System.out.print("  ");
        }
        System.out.println();
    }

    private static void readTrainingDataFile() throws IOException {
        while (fin.readLine() != null) {
            String s;
            ArrayList<Integer> hids = new ArrayList<>();
            ArrayList<HashMap<String, String>> observs = new ArrayList<>();
            hids.add(0);
            HashMap<String, String> attrs = new HashMap<>();
            attrs.put("node", "");
            attrs.put("path", "<html>");
            observs.add(attrs);
            while (!(s = fin.readLine()).equals("__EOS__")) {
                String[] content = s.split("\t");
                hids.add(Integer.valueOf(content[0]));
                attrs.clear();
                for (int i = 1; i < content.length; i++) {
                    String[] attr = content[i].split("\\[[0-9]*\\]=");
                    attrs.put(attr[0], attr[1]);
                }
                observs.add(attrs);
            }
            hids.add(0);
            attrs.clear();
            attrs.put("node", "");
            attrs.put("path", "<html>");
            observs.add(attrs);
            hiddenValues.add(hids);
            observations.add(observs);
        }
    }
}

//C:\Users\KatyaKos\Desktop\NIR-2017\crf_impl\test\test_file.txt