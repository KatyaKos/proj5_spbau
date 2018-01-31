package crf;

import com.expleague.commons.seq.CharSeqTools;
import crf.utils.Attributes;
import crf.utils.Constants;
import crf.utils.Labels;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class CrfFunctions {

    public final HashSet<String>[] sortedTags;
    public final ArrayList<String> allTags;
    private final int allTagsSize;
    private final int extraG = Constants.EXTRA_G_FUNCTIONS;
    private final int extraF = Constants.EXTRA_F_FUNCTIONS;
    private final int defaultLabel = Labels.getInstance().defaultLabel;

    private CrfFunctions(HashSet<String>[] sortedTags, ArrayList<String> allTags) {
        this.sortedTags = sortedTags;
        this.allTags = allTags;
        this.allTagsSize = allTags.size();
    }

    public double getFFunction(int type, int prev_y, int y, Attributes.Attribute attr) {
        final String path = attr.path;
        String tag = allTags.get(type - extraF);
        if (sortedTags[y].contains(tag)){
            if (prev_y == y) return isTag(path, tag) ? 1 : 0;
            else return isTag(path, tag) ? 1 : 0;
        } else return (y == defaultLabel) ? 1 : 0;
    }

    public double getGFunction(int type, int y, Attributes.Attribute attr) {
        final String text = attr.text;
        final String path = attr.path;
        if (type == 0) return isNumeric(text) ? 1 : 0;
        if (type == 1) return isUpperCase(text) ? 1 : 0;
        if (type == 2) return isTime(text) ? 1 : 0;
        if (type == 3) return isDate(text) ? 1 : 0;
        if (type < extraG + allTagsSize) {
            String tag = allTags.get(type - extraG);
            if (sortedTags[y].contains(tag))
                return isTag(path, tag) ? 1 : 0;
            else return (y == defaultLabel) ? 1 : 0;
        }
        return 0d;
    }

    private TObjectIntMap<String> numericCache = new TObjectIntHashMap<>();
    private boolean isNumeric(String s) {
        //return pattern.matcher(s).matches();
        int cached = numericCache.get(s);
        if (cached != numericCache.getNoEntryValue())
            return cached > 0;
        boolean result = s.chars().parallel().noneMatch(x -> x != ',' && x != '.' && Character.isDigit(x));
        numericCache.put(s, result ? 1 : -1);
        return result;
    }

    private TObjectIntMap<String> upperCache = new TObjectIntHashMap<>();
    private boolean isUpperCase(String s) {
        int cached = upperCache.get(s);
        if (cached != numericCache.getNoEntryValue())
            return cached > 0;
        boolean result = s.toUpperCase().equals(s);
        upperCache.put(s, result ? 1 : -1);
        return result;
    }

    private Map<String, TObjectIntMap<String>> tagCache = new HashMap<>();
    private boolean isTag(String s, String match) {
        TObjectIntMap<String> known = tagCache.computeIfAbsent(match, k -> new TObjectIntHashMap<>());
        int result = known.get(s);
        if (result != known.getNoEntryValue())
            return result > 0;
        CharSequence[] tags = CharSeqTools.split(s, "/");
        int n = tags.length;
        for (int i = n - Constants.TAGS_NUMBER; i < n; i++) {
            if (i >= 0 && tags[i].equals(match)) {
                result = 1;
                break;
            }
        }
        if (result == 0)
            result = -1;
        known.put(s, result);
        tagCache.put(match, known);
        return result > 0;
    }

    private TObjectIntMap<String> timeCache = new TObjectIntHashMap<>();
    private boolean isTime(String s) {
        int cached = timeCache.get(s);
        if (cached != timeCache.getNoEntryValue())
            return cached > 0;
        boolean result = false;
        for (Pattern pattern : Constants.TIME_PATTERNS) {
            if (pattern.matcher(s).matches()) {
                result = true;
                break;
            }
        }
        timeCache.put(s, result ? 1 : -1);
        return result;
    }

    private TObjectIntMap<String> dateCache = new TObjectIntHashMap<>();
    private boolean isDate(String s) {
        int cached = dateCache.get(s);
        if (cached != dateCache.getNoEntryValue())
            return cached > 0;
        boolean result = false;
        for (DateFormat pattern : Constants.DATE_PATTERNS) {
            try {
                pattern.parse(s);
                result = true;
                break;
            } catch (ParseException e) {
                continue;
            }
        }
        dateCache.put(s, result ? 1 : -1);
        return result;
    }

    private static volatile CrfFunctions instance;

    public static CrfFunctions getInstance() {
        CrfFunctions localInstance = instance;
        if (localInstance == null) {
            throw new RuntimeException();
        }
        return localInstance;
    }

    public static CrfFunctions getInstance(HashSet<String>[] sortedTags, ArrayList<String> allTags) {
        CrfFunctions localInstance = instance;
        if (localInstance == null) {
            synchronized (CrfFunctions.class) {
                localInstance = instance;
                if (localInstance == null) {
                    localInstance = instance = new CrfFunctions(sortedTags, allTags);
                }
            }
        } else {
            throw new RuntimeException();
        }
        return localInstance;
    }
}
