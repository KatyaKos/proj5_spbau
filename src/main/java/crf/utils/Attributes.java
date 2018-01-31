package crf.utils;

import java.util.ArrayList;

public final class Attributes {

    private ArrayList<Attribute> attributes = new ArrayList<>();

    public void addAttribute(String text, String path) {
        attributes.add(new Attribute(text, path));
    }

    public Attribute getAttribute(int i) {
        return attributes.get(i);
    }

    public int size() {
        return attributes.size();
    }

    public static final class Attribute {
        public final String text;
        public final String path;

        private Attribute(String text, String path) {
            this.text = text;
            this.path = path;
        }
    }
}
