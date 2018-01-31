package crf.utils;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.List;

public class Labels {
    private TIntObjectHashMap<String> labelsIS = new TIntObjectHashMap<>();
    private TObjectIntHashMap<String> labelsSI = new TObjectIntHashMap<>();
    private int size = 1;
    public final int defaultLabel = 1;

    private Labels() {
        labelsSI.put("", 1);
        labelsIS.put(1, "");
        size++;
    }

    public void init(List<String> labels) {
        for (String l : labels) {
            this.labelsIS.put(size, l);
            this.labelsSI.put(l, size);
            size++;
        }
    }

    public int getLabelsSize() {
        return size;
    }

    public String getLabelByNumber(int i) {
        return labelsIS.get(i);
    }

    public int getNumberByLabel(String label) {
        return labelsSI.get(label);
    }

    private static volatile Labels instance;

    public static Labels getInstance() {
        Labels localInstance = instance;
        if (localInstance == null) {
            synchronized (Labels.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Labels();
                }
            }
        }
        return localInstance;
    }
}
