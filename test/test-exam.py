# -- coding: utf-8 --
# -- coding: utf-8 --
import os


def GetProgressBar(count=0, totalCount=1, name=""):
    bar = "|"
    plusCount = 50 * count // totalCount
    for _ in range(plusCount):
        bar = bar + ">"
    for _ in range(50 - plusCount):
        bar = bar + "-"
    bar = bar + "|" + str(round(100 * count / totalCount, 2)).rjust(10) + "%" + name.rjust(50)
    return bar


testFileDir = ".\\exam"

jarDir = "..\\out\\artifacts\\SysY_MIPS_Compiler_jar\\SysY-MIPS-Compiler.jar"
jarDir_unop = "..\\out\\artifacts\\SysY_MIPS_Compiler_jar\\SysY-MIPS-Compiler-unop.jar"
jarDir_op = "..\\out\\artifacts\\SysY_MIPS_Compiler_jar\\SysY-MIPS-Compiler-op.jar"
outputDir = ".\\exam-out"

mipsFile = "mips.asm"

if __name__ == '__main__':
    totalDataCount = 0
    for dataDirRoot, dataDirDirs, dataDirFiles in os.walk(testFileDir):
        for fileName in dataDirFiles:
            if (fileName[-4:] == ".txt" and fileName[0:8] == "testfile"):
                totalDataCount = totalDataCount + 1

    if totalDataCount == 0:
        print("No test data!")
        exit()
    os.system("copy " + jarDir_unop + " SysY-MIPS-Compiler.jar")
    print("total test data count: " + str(totalDataCount))
    print(GetProgressBar(name="testing ..."))
    correctCount = 0
    count = 0
    for dataDirRoot, dataDirDirs, dataDirFiles in os.walk(testFileDir):
        for fileName in dataDirFiles:
            if (fileName[-4:] == ".txt" and fileName[0:8] == "testfile" ):
                inFile = os.path.join(dataDirRoot, fileName)



                print(GetProgressBar(count, totalDataCount, inFile))



                os.system("copy " + os.path.join(dataDirRoot, fileName) + " testfile.txt")
                os.system("copy " + os.path.join(dataDirRoot, fileName) + " ..\\testfile.txt")
                os.system("java -jar SysY-MIPS-Compiler.jar")
                os.system("copy mips.txt mips.asm")
                os.system("copy mips.txt " + os.path.join(outputDir, fileName[0:9]+"_20373091_龚悦_优化前目标代码.txt"))
                os.system("copy llvm_ir.txt "+ os.path.join(outputDir, fileName[0:9]+"_20373091_龚悦_优化前中间代码.txt"))
                # if(os.path.exists("llvm_ir_optimize.txt")):
                #     os.system("copy llvm_ir_optimize.txt ..\\llvm_ir_optimize.txt")
                #stdFile = ".\\Syntax-analysis"+ dataDirRoot[16:]
                print("Compiled finished, running testfile")
                inputFile = fileName.replace("testfile", "input")
                std_out_file = fileName.replace("testfile", "output")
                os.system("copy " + os.path.join(dataDirRoot, inputFile) + " input.txt")
                os.system("copy " + os.path.join(dataDirRoot, inputFile) + " ..\\input.txt")
                os.system("copy " + os.path.join(dataDirRoot, std_out_file) + " output.txt")
                os.system("copy " + os.path.join(dataDirRoot, std_out_file) + " ..\\output.txt")

                os.system("java -jar mars.jar nc mc Default mips.asm < input.txt > my_output.txt")



                if (os.system("fc my_output.txt output.txt")):
                    print("Wrong Answer in " + os.path.join(dataDirRoot, fileName))
                    exit()

                count = count + 1

    print(GetProgressBar(totalDataCount, totalDataCount, "Done."))
    print("Accept.")

    totalDataCount = 0
    for dataDirRoot, dataDirDirs, dataDirFiles in os.walk(testFileDir):
        for fileName in dataDirFiles:
            if (fileName[-4:] == ".txt" and fileName[0:8] == "testfile"):
                totalDataCount = totalDataCount + 1

    if totalDataCount == 0:
        print("No test data!")
        exit()
    os.system("copy " + jarDir_op + " SysY-MIPS-Compiler.jar")
    print("total test data count: " + str(totalDataCount))
    print(GetProgressBar(name="testing ..."))
    correctCount = 0
    count = 0
    for dataDirRoot, dataDirDirs, dataDirFiles in os.walk(testFileDir):
        for fileName in dataDirFiles:
            if (fileName[-4:] == ".txt" and fileName[0:8] == "testfile"):
                inFile = os.path.join(dataDirRoot, fileName)

                print(GetProgressBar(count, totalDataCount, inFile))

                os.system("copy " + os.path.join(dataDirRoot, fileName) + " testfile.txt")
                os.system("copy " + os.path.join(dataDirRoot, fileName) + " ..\\testfile.txt")
                os.system("java -jar SysY-MIPS-Compiler.jar")
                os.system("copy mips.txt mips.asm")
                os.system("copy mips.txt " + os.path.join(outputDir, fileName[0:9] + "_20373091_龚悦_优化后目标代码.txt"))
                #os.system("copy llvm_ir.txt " + os.path.join(outputDir, fileName[0:9] + "_20373091_龚悦_优化前中间代码"))
                if(os.path.exists("llvm_ir_optimize.txt")):
                    os.system("copy llvm_ir_optimize.txt "+ os.path.join(outputDir, fileName[0:9] + "_20373091_龚悦_优化后中间代码.txt"))
                # stdFile = ".\\Syntax-analysis"+ dataDirRoot[16:]
                print("Compiled finished, running testfile")
                inputFile = fileName.replace("testfile", "input")
                std_out_file = fileName.replace("testfile", "output")
                os.system("copy " + os.path.join(dataDirRoot, inputFile) + " input.txt")
                os.system("copy " + os.path.join(dataDirRoot, inputFile) + " ..\\input.txt")
                os.system("copy " + os.path.join(dataDirRoot, std_out_file) + " output.txt")
                os.system("copy " + os.path.join(dataDirRoot, std_out_file) + " ..\\output.txt")

                os.system("java -jar mars.jar nc mc Default mips.asm < input.txt > my_output.txt")

                if (os.system("fc my_output.txt output.txt")):
                    print("Wrong Answer in " + os.path.join(dataDirRoot, fileName))
                    exit()

                count = count + 1

    print(GetProgressBar(totalDataCount, totalDataCount, "Done."))
    print("Accept.")

