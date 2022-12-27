import back.MipsObject;
import back.Translator;
import exception.CompileExc;
import front.CompileUnit;
import front.Lexer;
import front.Parser;
import front.SemanticChecker;
import front.Token;
import front.TokenPackage;
import front.nodes.CompileUnitNode;
import mid.IrModule;
import mid.MidCodeGenerator;
import mid.optimize.IrOptimizer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Compiler {
    public static boolean OPTIMISER = true;

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
            System.out.println("error happened");
            PrintWriter outputStream = new PrintWriter(new FileOutputStream("error.txt"));
            Collections.sort(errs);
            for (CompileExc e : errs) {
                System.out.println(e);
                outputStream.println(e);
            }
            outputStream.close();
        }

    }

    public static void parserTest(String inputFile, String outputFile) throws Exception {
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenizeAutomata(sourceCode);
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
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        Set<CompileExc> errs = new HashSet<>(Parser.COMPILE_EXCS);
        CompileUnitNode compileUnitNode = SemanticChecker.buildCompileUnitNode(compileUnit);
        errs.addAll(SemanticChecker.getError());
        List<CompileExc> excs = new ArrayList<>(errs);
        IrModule irModule;
        MipsObject mipsObject;
        if (excs.size() == 0) {
            irModule = MidCodeGenerator.compileUnitToIr(compileUnitNode);
            //mipsObject = (new Translator(irModule)).toMips();
        } else {
            irModule = new IrModule();
            mipsObject = new MipsObject();
        }

        output("llvm_ir.txt", irModule.toIr(), excs);

    }

    public static void mipsTest(String inputFile, String outputFile) throws Exception {
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        Set<CompileExc> errs = new HashSet<>(Parser.COMPILE_EXCS);
        CompileUnitNode compileUnitNode = SemanticChecker.buildCompileUnitNode(compileUnit);
        errs.addAll(SemanticChecker.getError());
        List<CompileExc> excs = new ArrayList<>(errs);
        IrModule irModule;
        MipsObject mipsObject;
        if (excs.size() == 0) {
            irModule = MidCodeGenerator.compileUnitToIr(compileUnitNode);
            mipsObject = (new Translator(irModule)).toMips();
        } else {
            System.out.println("error happened");
            irModule = new IrModule();
            mipsObject = new MipsObject();
        }
        output("llvm_ir.txt", irModule.toIr(), excs);
        output("mips.txt", mipsObject.toMips(), excs);

    }

    public static void optimizeTest(String inputFile, String outPutFile) throws Exception {
        OPTIMISER = true;
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        Set<CompileExc> errs = new HashSet<>(Parser.COMPILE_EXCS);
        CompileUnitNode compileUnitNode = SemanticChecker.buildCompileUnitNode(compileUnit);
        errs.addAll(SemanticChecker.getError());
        List<CompileExc> excs = new ArrayList<>(errs);
        IrModule irModule;
        MipsObject mipsObject;
        if (excs.size() == 0) {
            MidCodeGenerator.setOptimizer(OPTIMISER);
            Translator.setOptimizer(OPTIMISER);
            irModule = MidCodeGenerator.compileUnitToIr(compileUnitNode);
            output("llvm_ir.txt", irModule.toIr(), excs);
            irModule = IrOptimizer.optimize(irModule);

            mipsObject = (new Translator(irModule)).toMips();
        } else {
            System.out.println("error happened");
            irModule = new IrModule();
            mipsObject = new MipsObject();
        }
        output("llvm_ir_optimize.txt", irModule.toIr(), excs);
        output("mips.txt", mipsObject.toMips(), excs);
    }

    public static void returnTest(String inputFile, String outputFile) throws Exception {
        String sourceCode = input(inputFile);
        List<Token> tokens = Lexer.tokenize(sourceCode);
        TokenPackage tokenPackage = new TokenPackage(tokens);
        CompileUnit compileUnit = Parser.parseCompUnit(tokenPackage);
        Set<CompileExc> errs = new HashSet<>(Parser.COMPILE_EXCS);
        CompileUnitNode compileUnitNode = SemanticChecker.buildCompileUnitNode(compileUnit);
        errs.addAll(SemanticChecker.getError());
        List<CompileExc> excs = new ArrayList<>(errs);
        output(outputFile, compileUnit.toString(), excs);
    }

    public static void main(String[] args) throws Exception {

        optimizeTest("testfile.txt", "output.txt");

    }
}
