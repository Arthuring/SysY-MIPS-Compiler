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


testFileDir = ".\\testfiles-only"

jarDir = "..\\out\\artifacts\\SysY_MIPS_Compiler_jar\\SysY-MIPS-Compiler.jar"

myOutFile = "output.txt"

if __name__ == '__main__':
    totalDataCount = 0
    for dataDirRoot, dataDirDirs, dataDirFiles in os.walk(testFileDir):
        for fileName in dataDirFiles:
            if (fileName[-4:] == ".txt" and fileName[0:8] == "testfile"):
                totalDataCount = totalDataCount + 1

    if totalDataCount == 0:
        print("No test data!")
        exit()
    os.system("copy " + jarDir + " SysY-MIPS-Compiler.jar")
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
                os.system("java -jar SysY-MIPS-Compiler.jar")
                #stdFile = ".\\Syntax-analysis"+ dataDirRoot[16:]

                stdFile = dataDirRoot.replace("testfiles-only","Syntax-analysis")
                #stdFile = dataDirRoot
                stdFile = os.path.join(stdFile, "output"+fileName[8:])
                ansFile = "ans.txt"
                print(stdFile)
                os.system("copy "+ stdFile + " " + ansFile)

                if (os.system("fc " + ansFile + " " + myOutFile)):
                    print("Wrong Answer in " + os.path.join(dataDirRoot, fileName))
                    exit()

                count = count + 1

    print(GetProgressBar(totalDataCount, totalDataCount, "Done."))
    print("Accept.")