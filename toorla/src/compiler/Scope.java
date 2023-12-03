package compiler;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    String name;
    int startLine;
    private Scope parent;
    int level;
    boolean printed;
    private HashMap<String, String> symbolTable = new HashMap<>();

    public Scope(String name, int startLine, Scope parent, int level) {
        this.name = name;
        this.startLine = startLine;
        this.parent = parent;
        this.level = level;
        printed = false;
    }

    public void setParent(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public void insert(String idefName, String attributes) {
        symbolTable.put(idefName, attributes);
    }

    public String lookup(String idefName) {
        return symbolTable.get(idefName);
    }

    public String printItems() {
        String itemsStr = "";
        for (Map.Entry<String, String> entry : symbolTable.entrySet()) {
            itemsStr += "Key : " + entry.getKey() + " | Value : " + entry.getValue()
                    + "\n";
        }
        return itemsStr;
    }

    public String toString() {
        return "------------- " + name + " : " + startLine + " -------------\n" +
                printItems() +
                "-----------------------------------------\n";
    }
}

