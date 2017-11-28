import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class CrfMain {

    private static ArrayList<Double> lambdas;
    private static ArrayList<Double> mus;

    private static ArrayList<ArrayList<Integer>> hidden = new ArrayList<>();
    private static ArrayList<ArrayList<HashMap<String, String>>> observations = new ArrayList<>();

    private static TrainCrf trainCrf;

    private static BufferedReader fin;

    public static void main(String[] args) throws IOException {
        train();
        label();
    }

    private static void train() throws IOException {
        Scanner inScanner = new Scanner(System.in);
        File file = new File(inScanner.nextLine());
        fin = new BufferedReader(new FileReader(file));
        readDataFile();
        trainCrf = new TrainCrf(hidden, observations);
        System.out.println("Start training.");
        long startTime = System.nanoTime();
        Pair<ArrayList<Double>, ArrayList<Double>> res = trainCrf.train();
        long endTime = System.nanoTime();
        System.out.println("Training took " + (endTime - startTime) + " milliseconds");
        lambdas = res.getKey();
        mus = res.getValue();
        printCoefficients();
    }

    private static void label() throws IOException {
        hidden = new ArrayList<>();
        observations = new ArrayList<>();
        File file = new File("C:\\Users\\KatyaKos\\Desktop\\NIR-2017\\crf_impl\\test\\test_file.txt");
        fin = new BufferedReader(new FileReader(file));
        readDataFile();
        System.out.println("Start testing.");
        long startTime = System.nanoTime();
        ArrayList<ArrayList<Integer>> result = trainCrf.labelData(observations);
        long endTime = System.nanoTime();
        System.out.println("Testing took " + (endTime - startTime) + " milliseconds");

        //Printing answer.
        for (int m = 0; m < result.size(); m++) {
            ArrayList<Integer> hids = result.get(m);
            ArrayList<HashMap<String, String>> observs = observations.get(m);
            String name = "C:\\Users\\KatyaKos\\Desktop\\NIR-2017\\crf_impl\\test\\res_" + String.valueOf(m) + ".txt";
            FileWriter fw = new FileWriter(new File(name));
            BufferedWriter fout = new BufferedWriter(fw);
            for (int i = 0; i < hids.size(); i++) {
                String s  = hids.get(i).toString() + "\t" + observs.get(i).get("node") + "\n";
                fout.write(s);
            }
            fout.close();
            fw.close();
        }
    }

    private static void printCoefficients() {
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

    private static void readDataFile() throws IOException {
        while (fin.readLine() != null) {
            String s;
            ArrayList<Integer> hids = new ArrayList<>();
            ArrayList<HashMap<String, String>> observs = new ArrayList<>();
            hids.add(-1);
            HashMap<String, String> attrs = new HashMap<>();
            attrs.put("node", "");
            attrs.put("path", "<html>");
            observs.add(attrs);
            while (!(s = fin.readLine()).equals("__EOS__")) {
                String[] content = s.split("\t");
                hids.add(Integer.valueOf(content[0]));
                attrs = new HashMap<>();
                for (int i = 1; i < content.length; i++) {
                    String[] attr = content[i].split("\\[[0-9]*\\]=");
                    attrs.put(attr[0], attr[1]);
                }
                observs.add(attrs);
            }
            hids.add(-1);
            attrs = new HashMap<>();
            attrs.put("node", "");
            attrs.put("path", "<html>");
            observs.add(attrs);
            hidden.add(hids);
            observations.add(observs);
        }
    }
}

//C:\Users\KatyaKos\Desktop\NIR-2017\crf_impl\test\train_file.txt