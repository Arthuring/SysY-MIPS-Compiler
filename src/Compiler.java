import exception.CompileExc;
import front.*;
import front.nodes.CompileUnitNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compiler {
    public static String input(String inputFile) throws Exception {
        InputStream inputStream = new FileInputStream(inputFile);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes);
    }

    public static void output(String outputFile, String outputString, List<CompileExc> errs) throws Exception {
        if (errs.isEmpty()) {
            PrintWriter outputStream = new PrintWriter(new FileOutputStream(outputFile));
            outputStream.println(outputString);
            outputStream.close();
        } else {
            PrintWriter outputStream = new PrintWriter(new FileOutputStream("error.txt"));
            Collections.sort(errs);
            for (CompileExc e : errs) {
                outputStream.println(e);
            }
            outputStream.close();
        }

    }

    public static void parserTest(String inputFile, String outputFile) throws Exception {
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        output(outputFile, compileUnit.toString(), Collections.emptyList());
    }

    public static void tokenizeTest(String inputFile, String outputFile) throws Exception {
        InputStream inputStream = new FileInputStream(inputFile);
        PrintWriter outputStream = new PrintWriter(new FileOutputStream(outputFile));
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String sourceCode = new String(bytes);
        List<Token> tokens = Lexer.tokenizeAutomata(sourceCode);
        for (Token token : tokens) {
            outputStream.println(token);
        }
        outputStream.close();
    }

    public static void irTest(String inputFile, String outputFile) throws Exception {
        List<CompileExc> errs = new ArrayList<>();
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        errs.addAll(Parser.COMPILE_EXCS);
        CompileUnitNode compileUnitNode = SyntaxTreeBuilder.buildCompileUnitNode(compileUnit);
        output(outputFile, compileUnitNode.toString(), errs);
    }

    public static void main(String[] args) throws Exception {

            tokenizeTest("testfile.txt", "output.txt");

    }
}
