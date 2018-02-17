import crf.CrfFunctions;
import crf.LabelCrf;
import crf.TrainCrf;
import crf.io.PrinterCrf;
import crf.io.ReaderCrf;
import crf.utils.Attributes;
import crf.utils.Coefficients;
import crf.utils.Constants;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("What do you want to do? train=1, label=2, both=3");
        final int task = (new Scanner(System.in)).nextInt();
        switch (task){
            case 1:
                train();
                break;
            case 2:
                label();
                break;
            case 3:
                trainAndLabel();
                break;
        }
    }

    private static Coefficients askSettings(int mus, int lambdas) throws IOException {
        System.out.println("Do you want to use old coefficients if it is possible?(y/n)");
        final String answer = (new Scanner(System.in)).nextLine();
        if (answer.equals("y")) {
            Coefficients coefficients = ReaderCrf.readCoefficients();
            if (coefficients.getMusNumber() == mus && coefficients.getLambdasNumber() == lambdas) {
                return  coefficients;
            } else System.err.println("Using old coefficients is not possible. Their number has changed.");
        }
        return null;
    }

    private static void train() throws IOException {
        List<TIntArrayList> hidden = ReaderCrf.readLabels(Constants.TRAIN_DATA_FILE);
        List<Attributes> observations = ReaderCrf.readAttributes(Constants.TRAIN_DATA_FILE);
        TrainCrf trainCrf = new TrainCrf(hidden, observations);
        Coefficients coefficients = askSettings(trainCrf.gNumber, trainCrf.fNumber);
        trainCrf.setCoefficients(coefficients);

        System.out.println("Start training.");
        long startTime = System.nanoTime();
        Coefficients res = trainCrf.train();
        long endTime = System.nanoTime();
        System.out.println("Training took " + (endTime - startTime) + " nanoseconds");
        PrinterCrf.printCoeffs(res);
        PrinterCrf.printTrainingTags(CrfFunctions.getInstance().sortedTags, CrfFunctions.getInstance().allTags);
    }

    private static void label() throws IOException {
        List<Attributes> observations = ReaderCrf.readAttributes(Constants.TEST_DATA_FILE);
        ReaderCrf.readAllLabels();
        HashSet<String>[][] sortedTags = ReaderCrf.readSortedTags();
        ArrayList<String> allTags = ReaderCrf.readAllTags();
        Coefficients coefficients = ReaderCrf.readCoefficients();
        System.out.println("Start testing.");
        long startTime = System.nanoTime();
        LabelCrf labelCrf = new LabelCrf(coefficients, sortedTags, allTags);
        List<TIntArrayList> result = labelCrf.labelData(observations);
        long endTime = System.nanoTime();
        System.out.println("Testing took " + (endTime - startTime) + " nanoseconds");
        PrinterCrf.printLabeling(result, observations);
    }

    private static void trainAndLabel() throws IOException {
        List<TIntArrayList> hidden = ReaderCrf.readLabels(Constants.TRAIN_DATA_FILE);
        List<Attributes> observations = ReaderCrf.readAttributes(Constants.TRAIN_DATA_FILE);
        TrainCrf trainCrf = new TrainCrf(hidden, observations);
        System.out.println("Start training.");
        long startTime = System.nanoTime();
        Coefficients res = trainCrf.train();
        long endTime = System.nanoTime();
        System.out.println("Training took " + (endTime - startTime) + " nanoseconds");
        PrinterCrf.printCoeffs(res);

        observations = ReaderCrf.readAttributes(Constants.TEST_DATA_FILE);
        System.out.println("Start testing.");
        startTime = System.nanoTime();
        LabelCrf labelCrf = new LabelCrf(trainCrf);
        List<TIntArrayList> result = labelCrf.labelData(observations);
        endTime = System.nanoTime();
        System.out.println("Testing took " + (endTime - startTime) + " nanoseconds");
        PrinterCrf.printLabeling(result, observations);
    }
}