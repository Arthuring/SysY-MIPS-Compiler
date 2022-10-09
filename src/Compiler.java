import front.*;

import java.io.*;
import java.util.Currency;
import java.util.List;

public class Compiler {
    public static String input(String inputFile) throws Exception {
        InputStream inputStream = new FileInputStream(inputFile);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes);
    }

    public static void output(String outputFile, String outputString) throws Exception {
        PrintWriter outputStream = new PrintWriter(new FileOutputStream(outputFile));
        outputStream.println(outputString);
        outputStream.close();
    }


    public static void parserTest(String inputFile, String outputFile) throws Exception {
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        output(outputFile, compileUnit.toString());
    }

    public static void tokenizeTest(String inputFile, String outputFile) throws Exception {
        InputStream inputStream = new FileInputStream(inputFile);
        PrintWriter outputStream = new PrintWriter(new FileOutputStream(outputFile));
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String sourceCode = new String(bytes);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        for (Token token : tokens) {
            outputStream.println(token);
        }
        outputStream.close();
    }

    public static void main(String[] args) {
        try {
            parserTest("testfile.txt", "output.txt");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
