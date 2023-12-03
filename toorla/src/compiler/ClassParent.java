package compiler;

import java.util.ArrayList;

public class ClassParent {
    public String className;
    public String parent;

    public ArrayList<String> classes = new ArrayList<>();
    public ArrayList<String> parents = new ArrayList<>();

    public ClassParent(String aClass, String parent) {
        this.className = aClass;
        this.parent = parent;
        classes.add(aClass);
        parents.add(parent);
    }


}
