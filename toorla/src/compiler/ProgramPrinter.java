package compiler;

import gen.ToorlaListener;
import gen.ToorlaParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.*;


public class ProgramPrinter implements ToorlaListener {

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    int errorCount = 0;
    ArrayList<Scope> scopes = new ArrayList<>();
    Scope programScope;
    Scope currentScope;

    ArrayList<String> fields = new ArrayList<>();
    ArrayList<String> methods = new ArrayList<>();
    ArrayList<String> classNames = new ArrayList<String>();

    ArrayList<String> variables = new ArrayList<String>();

    ArrayList<String> cps = new ArrayList<>();

    boolean isFirstClass = true;




    @Override
    public void enterProgram(ToorlaParser.ProgramContext ctx) {
        programScope =  new Scope("program", ctx.start.getLine(), null, 0);
//        scopes.add(programScope);
        String attributesFForParent;
        String name;
        String parent;

        for (int i = 0; i < ctx.getChildCount()-1; i++) {
            String isEntry = "False";

            if (ctx.children.get(i).getChild(0).getText().equals("entry")) {
                isEntry = "True";
                name = ctx.children.get(i).getChild(1).getChild(1).getText();
                if (ctx.children.get(i).getChild(1).getChild(2).getText().equals("inherits"))
                    parent = ctx.children.get(i).getChild(1).getChild(3).getText();
                else
                    parent = "[]";
            }
            else {
                name = ctx.children.get(i).getChild(1).getText();
                if (ctx.children.get(i).getChild(2).getText().equals("inherits"))
                    parent = ctx.children.get(i).getChild(3).getText();
                else
                    parent = "[]";
            }

             attributesFForParent = "Class (name: " + name + ") (parent: " + parent + ") (isEntry: " + isEntry + ")";
             programScope.insert("Class_" + name, attributesFForParent);
        }
        System.out.println(programScope);
    }

    @Override
    public void exitProgram(ToorlaParser.ProgramContext ctx) {
    }

    @Override
    public void enterClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        String name = ctx.className.getText();


        String parent = "";
        if (ctx.classParent != null) {
            parent = ctx.classParent.getText();
        }


        if (classNames.contains(name)) {
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            redefinedError(line, column, "Class", name);
            name = ctx.className.getText() + "_" + line + "_" + column;
        }

        if (!parent.equals("")){
            if(isFirstClass) {
                cps.add(name);
                cps.add(parent);
                isFirstClass = false;
            }
            else {
                cps.add(parent);
                if(isDuplicate(cps)){
                    int line = ctx.start.getLine();
                    int column = ctx.start.getCharPositionInLine();
                    String msg = "";
                    int index = 0;
                    for(String item : cps){
                        if (index == cps.size() - 1){
                            msg += "[" + item + "]";
                        }
                        else {
                            msg += "[" + item + "]" + "->";
                        }
                        index++;
                    }
                    loopInheritance(msg);
                }
            }
        }





        //phase3
        if (classNames.contains(name)) {
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            redefinedError(line, column, "Class", name);
            name = ctx.className.getText() + "_" + line + "_" + column;
        }

        Scope classScope = new Scope(name, ctx.start.getLine(), programScope, programScope.level+1);
        scopes.add(classScope);
        currentScope = classScope;



    }

    @Override
    public void exitClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        boolean scopeToPrint = true;
        int i = 1;

        while(scopeToPrint) {
            scopeToPrint = false;
            for (Scope scope : scopes) {
                if (scope.level == i) {
                    System.out.println(scope);
                    scope.printed = true;
                    scopeToPrint = true;
                }
            }
            i++;
        }
        scopes.removeIf(scope -> scope.printed);
    }

    @Override
    public void enterEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {
    }

    @Override
    public void exitEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {
    }

    @Override
    public void enterFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {
        String name = ctx.fieldName.getText();
        String type = ctx.fieldType.getText();

        //phase3
        if (fields.contains(name)) {
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            redefinedError(line, column, "Field", name);
            name = ctx.fieldName.getText() + "_" + line + "_" + column;
        }

        fields.add(ctx.fieldName.getText());

        //isDefined is always True in field declaration
        String attributesFForParent = "ClassField (name: " + name + ") (type: " + type + ", isDefined: True)";
        currentScope.insert("Field_" + name, attributesFForParent);

    }

    @Override
    public void exitFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {}

    @Override
    public void enterAccess_modifier(ToorlaParser.Access_modifierContext ctx) {}

    @Override
    public void exitAccess_modifier(ToorlaParser.Access_modifierContext ctx) {}

    @Override
    public void enterMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {


        String name = ctx.methodName.getText();
        String returnType = ctx.t.getText();
        String attributesForParent;
        String attributesForMethod;
        String key;

        //phase3
        if (methods.contains(name)) {
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            redefinedError(line, column, "Method", name);
            name = ctx.methodName.getText() + "_" + line + "_" + column;
        }
        methods.add(name);

        Scope methodScope = new Scope(name, ctx.start.getLine(), currentScope, currentScope.level+1);
        scopes.add(methodScope);
        currentScope = methodScope;


        //parameters
        String parameters = "";
        ArrayList<String> paramsName = new ArrayList<>();
        ArrayList<String> paramsTypes = new ArrayList<>();
        int size = ctx.children.size();
        int paramStart = 0, paramEnd = 0;
        int i;

        for (i = 0; i < size; i++) {
            if (ctx.children.get(i).getText().equals("(")) {
                paramStart = i;
                break;
            }
        }
        for (; i < size; i++) {
            if (ctx.children.get(i).getText().equals(")")) {
                paramEnd = i;
                break;
            }
        }
        for (i = paramStart + 1; i < paramEnd; i += 4) {
            paramsName.add(ctx.children.get(i).getText());
            paramsTypes.add(ctx.children.get(i + 2).getText());
        }

        for (i = 0; i < paramsName.size(); i++) {
            String paramName = paramsName.get(i);
            String paramType = paramsTypes.get(i);

            parameters += "[name: " + paramName + ", type: " + paramType +
                    ", index: " + (i+1) + "], ";

            // inserts an entry in this method's symbol table for each of its parameters
            attributesForMethod = "ParamField (name: " + paramName + ") (type: " +
                    paramType + ", isDefined: True)";
            methodScope.insert("Field_" + paramName, attributesForMethod);
        }

        if (paramsName.size() == 0)
            parameters = "[]";
        else
            parameters = parameters.substring(0, parameters.length() - 2);


        // determine if current method is a constructor or a regular method
        if (name.equals(currentScope.getParent().name)) {
            attributesForParent = "Constructor ";
            key = "Constructor_";
        }
        else {
            attributesForParent = "Method ";
            key = "Method_";
        }

        attributesForParent += "(name: " + name + ") (return type: [" + returnType +
                "]) (parameter list: " + parameters + ")";

        currentScope.getParent().insert(key + name, attributesForParent);
    }

    @Override
    public void exitMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {
        currentScope = currentScope.getParent();
    }

    @Override
    public void enterClosedStatement(ToorlaParser.ClosedStatementContext ctx) {
    }

    @Override
    public void exitClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    @Override
    public void enterClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        Scope conditionalScope = new Scope("if", ctx.start.getLine(), currentScope, currentScope.level+1);
        scopes.add(conditionalScope);
        currentScope = conditionalScope;
    }

    @Override
    public void exitClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        currentScope = currentScope.getParent();

    }

    @Override
    public void enterOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        Scope conditionalScope = new Scope("nested", ctx.start.getLine(), currentScope, currentScope.level+1);
        scopes.add(conditionalScope);
        currentScope = conditionalScope;
    }

    @Override
    public void exitOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        currentScope = currentScope.getParent();

    }

    @Override
    public void enterOpenStatement(ToorlaParser.OpenStatementContext ctx) {

    }

    @Override
    public void exitOpenStatement(ToorlaParser.OpenStatementContext ctx) {

    }

    @Override
    public void enterStatement(ToorlaParser.StatementContext ctx) {
    }

    @Override
    public void exitStatement(ToorlaParser.StatementContext ctx) {

    }

    @Override
    public void enterStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {
        String name = ctx.i1.getText();
        String exp = ctx.e1.getText();
        String type = "";

        if (exp.matches("([1-9][0-9]*)|[0]"))
            type = "int";
        else if (exp == "true" || exp == "false")
            type = "bool";
        else
            type = "string";

        if (variables.contains(name)) {
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            redefinedError(line, column, "Var", name);
            name = ctx.i1.getText() + "_" + line + "_" + column;
        }

        variables.add(ctx.i1.getText());

        //isDefined is always True in field declaration
        String attributesFForParent = "MethodVar (name: " + name + ") (type: localVar=" + type + ", isDefined: True)";
        currentScope.insert("Field_" + name, attributesFForParent);



//        List<TerminalNode> variables = ctx.ID();
//        for (TerminalNode variable : variables) {
//            String varDef = tabs + "field: " + variable + " / type: local var";
//            System.out.println(varDef);
//        }

    }

    @Override
    public void exitStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {

    }

    @Override
    public void enterStatementBlock(ToorlaParser.StatementBlockContext ctx) {
//        System.out.println(tabs + "nested{");
//        tabs.append("    ");
    }

    @Override
    public void exitStatementBlock(ToorlaParser.StatementBlockContext ctx) {
    }

    @Override
    public void enterStatementContinue(ToorlaParser.StatementContinueContext ctx) {

    }

    @Override
    public void exitStatementContinue(ToorlaParser.StatementContinueContext ctx) {

    }

    @Override
    public void enterStatementBreak(ToorlaParser.StatementBreakContext ctx) {

    }

    @Override
    public void exitStatementBreak(ToorlaParser.StatementBreakContext ctx) {

    }

    @Override
    public void enterStatementReturn(ToorlaParser.StatementReturnContext ctx) {

    }

    @Override
    public void exitStatementReturn(ToorlaParser.StatementReturnContext ctx) {

    }

    @Override
    public void enterStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        Scope conditionalScope = new Scope("while", ctx.start.getLine(), currentScope, currentScope.level+1);
        scopes.add(conditionalScope);
        currentScope = conditionalScope;
    }

    @Override
    public void exitStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        currentScope = currentScope.getParent();

    }

    @Override
    public void enterStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        Scope conditionalScope = new Scope("nested", ctx.start.getLine(), currentScope, currentScope.level+1);
        scopes.add(conditionalScope);
        currentScope = conditionalScope;
    }

    @Override
    public void exitStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        currentScope = currentScope.getParent();

    }

    @Override
    public void enterStatementWrite(ToorlaParser.StatementWriteContext ctx) {

    }

    @Override
    public void exitStatementWrite(ToorlaParser.StatementWriteContext ctx) {

    }

    @Override
    public void enterStatementAssignment(ToorlaParser.StatementAssignmentContext ctx) {
    }

    @Override
    public void exitStatementAssignment(ToorlaParser.StatementAssignmentContext ctx) {

    }

    @Override
    public void enterStatementInc(ToorlaParser.StatementIncContext ctx) {
    }

    @Override
    public void exitStatementInc(ToorlaParser.StatementIncContext ctx) {

    }

    @Override
    public void enterStatementDec(ToorlaParser.StatementDecContext ctx) {
    }

    @Override
    public void exitStatementDec(ToorlaParser.StatementDecContext ctx) {

    }

    @Override
    public void enterExpression(ToorlaParser.ExpressionContext ctx) {
    }

    @Override
    public void exitExpression(ToorlaParser.ExpressionContext ctx) {
    }

    @Override
    public void enterExpressionOr(ToorlaParser.ExpressionOrContext ctx) {

    }

    @Override
    public void exitExpressionOr(ToorlaParser.ExpressionOrContext ctx) {

    }

    @Override
    public void enterExpressionOrTemp(ToorlaParser.ExpressionOrTempContext ctx) {

    }

    @Override
    public void exitExpressionOrTemp(ToorlaParser.ExpressionOrTempContext ctx) {

    }

    @Override
    public void enterExpressionAnd(ToorlaParser.ExpressionAndContext ctx) {

    }

    @Override
    public void exitExpressionAnd(ToorlaParser.ExpressionAndContext ctx) {

    }

    @Override
    public void enterExpressionAndTemp(ToorlaParser.ExpressionAndTempContext ctx) {

    }

    @Override
    public void exitExpressionAndTemp(ToorlaParser.ExpressionAndTempContext ctx) {

    }

    @Override
    public void enterExpressionEq(ToorlaParser.ExpressionEqContext ctx) {

    }

    @Override
    public void exitExpressionEq(ToorlaParser.ExpressionEqContext ctx) {

    }

    @Override
    public void enterExpressionEqTemp(ToorlaParser.ExpressionEqTempContext ctx) {

    }

    @Override
    public void exitExpressionEqTemp(ToorlaParser.ExpressionEqTempContext ctx) {

    }

    @Override
    public void enterExpressionCmp(ToorlaParser.ExpressionCmpContext ctx) {

    }

    @Override
    public void exitExpressionCmp(ToorlaParser.ExpressionCmpContext ctx) {

    }

    @Override
    public void enterExpressionCmpTemp(ToorlaParser.ExpressionCmpTempContext ctx) {

    }

    @Override
    public void exitExpressionCmpTemp(ToorlaParser.ExpressionCmpTempContext ctx) {

    }

    @Override
    public void enterExpressionAdd(ToorlaParser.ExpressionAddContext ctx) {

    }

    @Override
    public void exitExpressionAdd(ToorlaParser.ExpressionAddContext ctx) {

    }

    @Override
    public void enterExpressionAddTemp(ToorlaParser.ExpressionAddTempContext ctx) {

    }

    @Override
    public void exitExpressionAddTemp(ToorlaParser.ExpressionAddTempContext ctx) {

    }

    @Override
    public void enterExpressionMultMod(ToorlaParser.ExpressionMultModContext ctx) {

    }

    @Override
    public void exitExpressionMultMod(ToorlaParser.ExpressionMultModContext ctx) {

    }

    @Override
    public void enterExpressionMultModTemp(ToorlaParser.ExpressionMultModTempContext ctx) {

    }

    @Override
    public void exitExpressionMultModTemp(ToorlaParser.ExpressionMultModTempContext ctx) {

    }

    @Override
    public void enterExpressionUnary(ToorlaParser.ExpressionUnaryContext ctx) {

    }

    @Override
    public void exitExpressionUnary(ToorlaParser.ExpressionUnaryContext ctx) {

    }

    @Override
    public void enterExpressionMethods(ToorlaParser.ExpressionMethodsContext ctx) {
    }

    @Override
    public void exitExpressionMethods(ToorlaParser.ExpressionMethodsContext ctx) {

    }

    @Override
    public void enterExpressionMethodsTemp(ToorlaParser.ExpressionMethodsTempContext ctx) {
    }

    @Override
    public void exitExpressionMethodsTemp(ToorlaParser.ExpressionMethodsTempContext ctx) {

    }

    @Override
    public void enterExpressionOther(ToorlaParser.ExpressionOtherContext ctx) {
//        ctx.n.getText()
    }

    @Override
    public void exitExpressionOther(ToorlaParser.ExpressionOtherContext ctx) {

    }

    @Override
    public void enterToorlaType(ToorlaParser.ToorlaTypeContext ctx) {
    }

    @Override
    public void exitToorlaType(ToorlaParser.ToorlaTypeContext ctx) {

    }

    @Override
    public void enterSingleType(ToorlaParser.SingleTypeContext ctx) {
    }

    @Override
    public void exitSingleType(ToorlaParser.SingleTypeContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }

    public void redefinedError(int line, int column, String type, String name) {
        name = " [%s]".formatted(name);
        errorCount++;
        System.out.println(ANSI_RED +
                "Error" + errorCount + " : in line[" + line + ":" + column + "] , "
                + type + name + " has been defined already"
                + ANSI_RESET);
    }

    public void loopInheritance(String message) {
//        message = " [%s]".formatted(message);
        errorCount++;
//        System.out.println(ANSI_RED +
//                "Error" + errorCount + " : in line[" + line + ":" + column + "] , "
//                + type + message + ANSI_RESET);

        System.out.println(ANSI_RED + "Error" + errorCount + " : " + "Invalid inheritance " + message + ANSI_RESET);
    }


    public boolean isDuplicate(ArrayList<String> classes){

        Set<String> set = new HashSet<>(classes);

        return set.size() < classes.size();
    }
}
