import com.expleague.commons.text.StringUtils;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class CrfMain {

    private static TDoubleArrayList lambdas;
    private static TDoubleArrayList mus;

    private static Pattern pattern = Pattern.compile("\\[[0-9]*\\]=");

    private static ArrayList<TIntArrayList> hidden = new ArrayList<>();
    private static List<List<Map<String, String>>> observations = new ArrayList<>();

    private static TrainCrf trainCrf;

    private static BufferedReader fin;

    public static void main(String[] args) throws IOException {
        train();
        label();
    }

    private static void train() throws IOException {
        Scanner inScanner = new Scanner(System.in);
        File file = new File("C:\\Users\\KatyaKos\\Desktop\\NIR-2017\\crf_impl\\test\\train_file.txt");
        fin = new BufferedReader(new FileReader(file));
        readDataFile();
        trainCrf = new TrainCrf(hidden, observations);
        System.out.println("Start training.");
        long startTime = System.nanoTime();
        Pair<TDoubleArrayList, TDoubleArrayList> res = trainCrf.train();
        long endTime = System.nanoTime();
        System.out.println("Training took " + (endTime - startTime) + " nanoseconds");
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
        List<TIntArrayList> result = trainCrf.labelData(observations);
        long endTime = System.nanoTime();
        System.out.println("Testing took " + (endTime - startTime) + " nanoseconds");

        //Printing answer.
        for (int m = 0; m < result.size(); m++) {
            TIntArrayList hids = result.get(m);
            List<Map<String, String>> observs = observations.get(m);
            String name = "C:\\Users\\KatyaKos\\Desktop\\NIR-2017\\crf_impl\\test\\res_" + String.valueOf(m) + ".txt";
            FileWriter fw = new FileWriter(new File(name));
            BufferedWriter fout = new BufferedWriter(fw);
            for (int i = 0; i < hids.size(); i++) {
                String s  = hids.get(i) + "\t" + observs.get(i).get("node") + "\t" + observs.get(i).get("path") + "\n";
                fout.write(s);
            }
            fout.close();
            fw.close();
        }
    }

    private static void printCoefficients() {
        System.out.print("Lambdas:  ");
        for (double lambda : lambdas.toArray()) {
            System.out.print(lambda);
            System.out.print("  ");
        }
        System.out.println();
        System.out.print("Mus:  ");
        for (double mu : mus.toArray()) {
            System.out.print(mu);
            System.out.print("  ");
        }
        System.out.println();
    }

    private static void readDataFile() throws IOException {
        while (fin.readLine() != null) {
            String s;
            TIntArrayList hids = new TIntArrayList();
            List<Map<String, String>> observs = new ArrayList<>();
            hids.add(-1);
            Map<String, String> attrs = new HashMap<>();
            attrs.put("node", "");
            attrs.put("path", "<html>");
            observs.add(attrs);
            while (!(s = fin.readLine()).equals("__EOS__")) {
                String[] content = StringUtils.split(s, "\t", 0);
                hids.add(Integer.valueOf(content[0]));
                attrs = new HashMap<>();
                for (int i = 1; i < content.length; i++) {
                    String[] attr = pattern.split(content[i]);
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