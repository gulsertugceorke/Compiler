package com.company;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class Main {

    public static void main(String[] args) {
        Parser P1 = new Parser(new Scanner("program1.txt"));
        P1.WriteParse();
        Parser P2 = new Parser(new Scanner("program1.txt"));
        P2.parse();
        Evaluator.eval(P2.getparseTree());
    }
}

class Evaluator {
    static Map<String, Integer> variableList = new HashMap<>();
    static String nodeData = "";

    static void eval(Node node) {
        switch(node.data) {
            case "assignment":
                String leftSide = "";
                for(Node n: node.subnodes) {
                    if(leftSide.equals(""))
                        leftSide = n.data;

                    if(n.data.equals("expression")) {
                        for(Node n2: n.subnodes) {
                            nodeData += n2.data + " ";
                        }
                    }
                }
                variableList.put(leftSide, operations(nodeData));
                nodeData = "";
                break;

            case "while":
                for(Node n: node.subnodes.get(0).subnodes) {
                    nodeData += n.data + " ";
                }
                while(operations(nodeData) != 0) {
                    for(Node n: node.subnodes.get(1).subnodes) {
                        eval(n);
                    }
                    for(Node n: node.subnodes.get(0).subnodes) {
                        nodeData += n.data + " ";
                    }
                }
                nodeData = "";
                break;

            case "output":
                nodeData = "";
                for(Node n: node.subnodes.get(0).subnodes) {
                    nodeData += n.data + " ";
                }
                System.out.println(operations(nodeData));
                nodeData = "";
                break;
        }
        for(Node n: node.subnodes) {
            eval(n);
        }
    }

    static int operations(String nodeData) {
        String[] pieces = nodeData.split("\\s+");
        int result = 1;
        if(pieces.length == 1) {
            if(isNumber(pieces[0])) {
                return Integer.parseInt(pieces[0]);
            }
            else {
                return variableList.get(pieces[0]);
            }
        }
        else {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            String ope = "";
            for(String p: pieces) {
                if(isNumber(p)) {
                    temp.add(Integer.parseInt(p));
                }
                else if(Character.isLetter(p.charAt(0))) {
                    temp.add(variableList.get(p));
                }
                else {
                    ope = p;
                }
                if(temp.size() > 1) {
                    switch(ope) {
                        case "+":
                            result = temp.get(0) + temp.get(1);
                            break;
                        case "-":
                            result = temp.get(0) - temp.get(1);
                            break;
                        case "*":
                            result = temp.get(0) * temp.get(1);
                            break;
                        case "/":
                            result = temp.get(0) / temp.get(1);
                            break;
                        case "^":
                            for(int i=0; i<temp.get(1); i++)
                                result *= temp.get(0);
                    }
                    temp.clear();
                    temp.add(result);
                }
            }
        }
        return result;
    }

    static boolean isNumber(String token) {
        try {
            int num = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}



class ParseTree {
    Node root;
    ParseTree() {
        root = new Node("root");
    }

    static void printTree(Node node) {
        System.out.println(node.data + " ");
        for(Node n: node.subnodes) {
            printTree(n);
        }
    }
}

class Node {
    ArrayList<Node> subnodes;
    String data;
    Node(String data) {
        subnodes = new ArrayList<Node>();
        this.data = data;
    }
}

class Scanner {
    private String progText;
    private int curPos = 0;
    Scanner(String fileName){
        try {
            byte [] allBytes = Files.readAllBytes(Paths.get(fileName));
            progText = new String(allBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    Token nextToken() {
        if(curPos == progText.length())
            return new EOFToken(null);

        while(Character.isWhitespace(progText.charAt(curPos))) {
            curPos++;
        }

        char curChar = progText.charAt(curPos);
        curPos++;

        if(Character.isDigit(curChar)){
            return new NumberToken(String.valueOf(curChar));
        }
        else if(Character.isAlphabetic(curChar)) {
            return new IdentifierToken(String.valueOf(curChar));
        }
        else if(curChar == '{') {
            return new WhileBegToken(String.valueOf(curChar));
        }
        else if(curChar == '}') {
            return new WhileEndToken(String.valueOf(curChar));
        }
        else if(curChar == '.') {
            return new Period(String.valueOf(curChar));
        }
        else if(curChar == '+') {
            return new Plus(String.valueOf(curChar));
        }
        else if(curChar == '-') {
            return new Minus(String.valueOf(curChar));
        }
        else if(curChar == '*') {
            return new Multiplication(String.valueOf(curChar));
        }
        else if(curChar == '/') {
            return new Division(String.valueOf(curChar));
        }
        else if(curChar == '%') {
            return new Modulus(String.valueOf(curChar));
        }
        else if(curChar == '^') {
            return new Exponent(String.valueOf(curChar));
        }
        else if(curChar == '(') {
            return new ParanthesBeg(String.valueOf(curChar));
        }
        else if(curChar == ')') {
            return new ParanthesEnd(String.valueOf(curChar));
        }
        else if(curChar == '[') {
            return new IfBeginning(String.valueOf(curChar));
        }
        else if(curChar == ']') {
            return new IfEnding(String.valueOf(curChar));
        }
        else if(curChar == '<') {
            return new Output(String.valueOf(curChar));
        }
        else if(curChar == '>') {
            return new Input(String.valueOf(curChar));
        }
        else if(curChar == ':') {
            return new Colon(String.valueOf(curChar));
        }
        else if(curChar == '?') {
            return new QuestionMark(String.valueOf(curChar));
        }
        else if(curChar == ';') {
            return new SemiColon(String.valueOf(curChar));
        }
        else if(curChar == '=') {
            return new Assignment(String.valueOf(curChar));
        }
        return new ErrorToken("NotRecognizedToken");
    }
}
class Parser{
    private Scanner scanner;
    private Token curToken;
    private ParseTree parseTree;

    boolean inCondition = false;

    Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    void WriteParse() {
        Token token = scanner.nextToken();
        while(!token.getType().equals(TokenType.END_OF_FILE)) {
            System.out.printf("token text: %s, token type: %s\n",token.text,token.tokenType.text);
            token = scanner.nextToken();
        }
        System.out.println("\n\nDone with the Program");
    }

    //checking if the program is syntactically right
    void parse() {
        curToken = scanner.nextToken();
        if(curToken.getText().equals(".")) {
            System.exit(0);
        }
        else {
            parseTree = new ParseTree();
            S(parseTree.root);
        }
        if(curToken.getType().equals(TokenType.END_OF_FILE)) {
            System.out.println("The program is syntatically correct");
            //ParseTree.printTree(parseTree.root);
        }
    }

    void S(Node tempNode) {
        if(curToken.getType().equals(TokenType.IF_BEG)) {
            inCondition = true;
            C(tempNode);
            return;
        }
        else if(curToken.getType().equals(TokenType.WHILE_BEG)) {
            inCondition = true;
            tempNode.subnodes.add(new Node("while"));
            W(tempNode.subnodes.get(tempNode.subnodes.size()-1));

            if(curToken.getType().equals(TokenType.WHILE_END)) {
                curToken = scanner.nextToken();
            }
            else {
                System.out.println("Error: '}' is expected!");
                System.exit(0);
            }
            return;
        }
        else if(curToken.getType().equals(TokenType.IDENTIFIER)) {
            tempNode.subnodes.add(new Node("assignment"));
            A(tempNode.subnodes.get(tempNode.subnodes.size()-1));
            if(!curToken.getType().equals(TokenType.END_OF_FILE)) {
                S(tempNode);
            }
            return;
        }
        else if(curToken.getType().equals(TokenType.NUMBER)) {
            tempNode.subnodes.add(new Node("assignment"));
            A(tempNode.subnodes.get(tempNode.subnodes.size()-1));
            if(!curToken.getType().equals(TokenType.END_OF_FILE)) {
                S(tempNode);
            }
            return;
        }
        else if(curToken.getType().equals(TokenType.OUTPUT)) {
            tempNode.subnodes.add(new Node("output"));
            O(tempNode.subnodes.get(tempNode.subnodes.size()-1));

            if(!curToken.getType().equals(TokenType.END_OF_FILE)) {
                S(tempNode);
            }
            return;
        }
        else if(curToken.getType().equals(TokenType.INPUT)) {
            tempNode.subnodes.add(new Node("input"));
            I(tempNode.subnodes.get(tempNode.subnodes.size()-1));

            if(!curToken.getType().equals(TokenType.END_OF_FILE)) {
                S(tempNode);
            }
            return;
        }
        else {
            if(!inCondition) {
                System.out.println("Syntax Error");
                System.exit(0);
            }
        }
    }
    void C(Node tempNode) {
        curToken = scanner.nextToken();
        E(tempNode);

        if(curToken.getType().equals(TokenType.QUESTION_MARK)) {
            curToken = scanner.nextToken();
            S(tempNode);

            while(!curToken.getType().equals(TokenType.COLON)
                    && !curToken.getType().equals(TokenType.IF_END)) {
                S(tempNode);
            }
            if(curToken.getType().equals(TokenType.COLON)) {
                S(tempNode);
                while(!curToken.getType().equals(TokenType.IF_END)) {
                    S(tempNode);
                }
                inCondition = false;
                curToken = scanner.nextToken();
            }
        }
        else {
            System.out.println("ERROR! '?' is expected after expression");
            System.exit(0);
        }
    }
    void W(Node tempNode) {
        curToken = scanner.nextToken();

        tempNode.subnodes.add(new Node("expression"));
        E(tempNode.subnodes.get(tempNode.subnodes.size()-1));

        if(curToken.getType().equals(TokenType.QUESTION_MARK)) {
            curToken = scanner.nextToken();

            tempNode.subnodes.add(new Node("body"));
            S(tempNode.subnodes.get(tempNode.subnodes.size()-1));

            while(!curToken.getType().equals(TokenType.WHILE_END) &&
                    !curToken.getType().equals(TokenType.END_OF_FILE)) {
                S(tempNode);
            }
            inCondition = false;
        }
        else {
            System.out.println("ERROR! '?' is expected after expression");
            System.exit(0);
        }
    }
    void A(Node tempNode) {
        L(tempNode);
        if(curToken.getType().equals(TokenType.Assignment)) {
            tempNode.subnodes.add(new Node(curToken.getText()));

            curToken = scanner.nextToken();

            tempNode.subnodes.add(new Node("expression"));
            E(tempNode.subnodes.get(tempNode.subnodes.size()-1));

            if(curToken.getType().equals(TokenType.SEMI_COLON)) {
                curToken = scanner.nextToken();
            }
            else {
                System.out.println("ERROR: ';' is expected instead of '" + curToken.getText() + "'");
                System.exit(0);
            }
        }
        else {
            System.out.println("ERROR: '=' is expected!");
            System.exit(0);
        }

    }
    void O(Node tempNode) {
        tempNode.subnodes.add(new Node("expression"));
        curToken = scanner.nextToken();
        E(tempNode.subnodes.get(tempNode.subnodes.size()-1));
        if(curToken.getType().equals(TokenType.SEMI_COLON)) {
            curToken = scanner.nextToken();
        }
        else {
            System.out.println("ERROR: ';' is expected!");
            System.exit(0);
        }
    }
    void I(Node tempNode) {
        curToken = scanner.nextToken();
        L(tempNode);
        if(curToken.getType().equals(TokenType.SEMI_COLON)) {
            curToken = scanner.nextToken();
        }
        else {
            System.out.println("ERROR: ';' is expected!");
            System.exit(0);
        }
    }
    void E(Node tempNode) {
        T(tempNode);
        while(curToken.getType().equals(TokenType.PLUS) ||
                curToken.getType().equals(TokenType.MINUS)) {

            if(curToken.getType().equals(TokenType.PLUS)) {
                tempNode.subnodes.add(new Node(curToken.getText()));
                curToken = scanner.nextToken();
                T(tempNode);
            }
            else if(curToken.getType().equals(TokenType.MINUS)) {
                tempNode.subnodes.add(new Node(curToken.getText()));
                curToken = scanner.nextToken();
                T(tempNode);
            }
        }
    }
    void T(Node tempNode) {
        U(tempNode);
        while(curToken.getType().equals(TokenType.MULTIPLICATION) ||
                curToken.getType().equals(TokenType.DIVISION) ||
                curToken.getType().equals(TokenType.MODULUS)) {

            if(curToken.getType().equals(TokenType.MULTIPLICATION)) {
                tempNode.subnodes.add(new Node(curToken.getText()));
                curToken = scanner.nextToken();
                U(tempNode);
            }
            else if(curToken.getType().equals(TokenType.DIVISION)) {
                tempNode.subnodes.add(new Node(curToken.getText()));
                curToken = scanner.nextToken();
                U(tempNode);
            }
            else if(curToken.getType().equals(TokenType.MODULUS)) {
                tempNode.subnodes.add(new Node(curToken.getText()));
                curToken = scanner.nextToken();
                U(tempNode);
            }
        }
    }
    void U(Node tempNode) {
        F(tempNode);
        if(curToken.getType().equals(TokenType.EXPONENT)) {
            tempNode.subnodes.add(new Node(curToken.getText()));
            curToken = scanner.nextToken();
            U(tempNode);
        }
    }
    void F(Node tempNode) {
        if(curToken.getType().equals(TokenType.PARANTHESES_BEG)) {
            curToken = scanner.nextToken();
            E(tempNode);
            curToken = scanner.nextToken();
            if(curToken.getType().equals(TokenType.PARANTHESES_END)) {
                curToken = scanner.nextToken();
            }
            else {
                System.out.println(" ERROR: ')' is expected!");
                System.exit(0);
            }
        }
        else if(curToken.getType().equals(TokenType.IDENTIFIER)) {
            L(tempNode);
        }
        else if(curToken.getType().equals(TokenType.NUMBER)) {
            D(tempNode);
        }
        else {
            System.out.println("Expression expected!");
            System.exit(0);
        }
    }
    void D(Node tempNode) {
        tempNode.subnodes.add(new Node(curToken.getText()));
        curToken = scanner.nextToken();
    }
    void L(Node tempNode) {
        tempNode.subnodes.add(new Node(curToken.getText()));
        curToken = scanner.nextToken();
    }
    Node getparseTree() {
        return parseTree.root;
    }
}

class Token{
    protected String text;
    protected TokenType tokenType;
    Token(String text){
        this.text = text;
    }
    TokenType getType() {
        return tokenType;
    }
    String getText() {
        return text;
    }

}
class EOFToken extends Token{
    EOFToken(String text){
        super("EOF");
        this.tokenType = TokenType.END_OF_FILE;
    }
}
class NumberToken extends Token{
    NumberToken(String text){
        super(text);
        this.tokenType = TokenType.NUMBER;
    }
}
class IdentifierToken extends Token{
    IdentifierToken(String text){
        super(text);
        this.tokenType = TokenType.IDENTIFIER;
    }
}
class WhileBegToken extends Token{
    WhileBegToken(String text){
        super(text);
        this.tokenType = TokenType.WHILE_BEG;
    }
}
class ErrorToken extends Token{
    ErrorToken(String text){
        super(text);
        this.tokenType = TokenType.NRT_ERROR;
    }
}
class WhileEndToken extends Token{
    WhileEndToken(String text){
        super(text);
        this.tokenType = TokenType.WHILE_END;
    }
}
class Period extends Token{
    Period(String text){
        super(text);
        this.tokenType = TokenType.PERIOD;
    }
}
class Colon extends Token{
    Colon(String text){
        super(text);
        this.tokenType = TokenType.COLON;
    }
}
class ParanthesBeg extends Token{
    ParanthesBeg(String text) {
        super(text);
        this.tokenType = TokenType.PARANTHESES_BEG;
    }
}

class ParanthesEnd extends Token{
    ParanthesEnd(String text) {
        super(text);
        this.tokenType = TokenType.PARANTHESES_END;
    }
}
class IfBeginning extends Token{
    IfBeginning(String text) {
        super(text);
        this.tokenType = TokenType.IF_BEG;
    }
}
class IfEnding extends Token{
    IfEnding(String text) {
        super(text);
        this.tokenType = TokenType.IF_END;
    }
}
class SemiColon extends Token{
    SemiColon(String text) {
        super(text);
        this.tokenType = TokenType.SEMI_COLON;
    }
}
class QuestionMark extends Token{
    QuestionMark(String text) {
        super(text);
        this.tokenType = TokenType.QUESTION_MARK;
    }
}
class Assignment extends Token{
    Assignment(String text) {
        super(text);
        this.tokenType = TokenType.Assignment;
    }
}
class Input extends Token{
    Input(String text) {
        super(text);
        this.tokenType = TokenType.INPUT;
    }
}
class Output extends Token{
    Output(String text) {
        super(text);
        this.tokenType = TokenType.OUTPUT;
    }
}
class Plus extends Token{
    Plus(String text) {
        super(text);
        this.tokenType = TokenType.PLUS;
    }
}
class Minus extends Token{
    Minus(String text) {
        super(text);
        this.tokenType = TokenType.MINUS;
    }
}
class Multiplication extends Token{
    Multiplication(String text) {
        super(text);
        this.tokenType = TokenType.MULTIPLICATION;
    }
}
class Division extends Token{
    Division(String text) {
        super(text);
        this.tokenType = TokenType.DIVISION;
    }
}
class Modulus extends Token{
    Modulus(String text) {
        super(text);
        this.tokenType = TokenType.MODULUS;
    }
}
class Exponent extends Token{
    Exponent(String text) {
        super(text);
        this.tokenType = TokenType.EXPONENT;
    }
}
enum TokenType{
    IF_BEG("["), IF_END("]"), WHILE_BEG("{"), WHILE_END("}"),
    SEMI_COLON(";"),QUESTION_MARK("?"), Assignment("="),INPUT(">"),
    OUTPUT("<"),PLUS("+"),MINUS("-"),MULTIPLICATION("*"),
    DIVISION("/"),MODULUS("%"),EXPONENT("^"),COLON(":"),PARANTHESES_BEG("("), PARANTHESES_END(")"),
    PERIOD,NUMBER,IDENTIFIER,NRT_ERROR, END_OF_FILE;
    String text;
    TokenType(){
        this.text = this.toString();
    }
    TokenType(String text){
        this.text = text;
    }
}