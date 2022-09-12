import front.Lexer;
import front.Token;

import java.io.*;
import java.util.List;

public class Compiler {
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
            tokenizeTest("testfile.txt", "output.txt");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
