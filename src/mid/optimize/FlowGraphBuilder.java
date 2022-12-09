package mid.optimize;

import mid.IrModule;
import mid.ircode.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class FlowGraphBuilder {
    private static FuncDef currentFunc;
    private static Map<Integer, BasicBlock> id2Block;
    private static Map<String, BasicBlock> label2Block;
    private static boolean DBG_MODE = false;
    private static String entryLabel = "entry";

    public static IrModule buildFlowGraph(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            currentFunc = funcDef;
            id2Block = funcDef.getId2Block();
            label2Block = funcDef.getLabel2Block();
            for (BasicBlock basicBlock : currentFunc.getBasicBlocks()) {
                id2Block.put(basicBlock.getId(), basicBlock);
                label2Block.put(basicBlock.getLabel(), basicBlock);
            }

            for (int i = 0; i < currentFunc.getBasicBlocks().size(); i += 1) {
                if (i == 0) {
                    currentFunc.getBasicBlocks().get(i).addPrevBlock(entryLabel);
                }
                if (i + 1 < currentFunc.getBasicBlocks().size()) {
                    connectBasicBlock(currentFunc.getBasicBlocks().get(i),
                            currentFunc.getBasicBlocks().get(i + 1));
                } else {
                    connectBasicBlock(currentFunc.getBasicBlocks().get(i), null);
                }
            }
            funcDef.setExitBlock(new BasicBlock(funcDef.getFuncEntry().name() + "_exit"));
            if (DBG_MODE) {
                System.out.println("FUNC:" + funcDef.getFuncEntry().name());
                for (BasicBlock basicBlock : currentFunc.getBasicBlocks()) {
                    System.out.println(basicBlock.getLabel() + ":");
                    System.out.println("PREV:\n" + basicBlock.getPrevBlock());
                    System.out.println("NEXT:\n" + basicBlock.getNextBlock());
                }
            }

        }
        return irModule;
    }

    public static void connectBasicBlock(BasicBlock basicBlock, BasicBlock nextBlock) {
        InstructionLinkNode lastInstr = basicBlock.getLastInstruction();
        if (lastInstr instanceof Jump) {
            String targetLabel = ((Jump) lastInstr).getTarget();
            basicBlock.addNextBlock(targetLabel);
            BasicBlock targetBlock = label2Block.get(targetLabel);
            targetBlock.addPrevBlock(basicBlock.getLabel());
        } else if (lastInstr instanceof Branch) {
            String targetLabel1 = ((Branch) lastInstr).getLabelTrue();
            String targetLabel2 = ((Branch) lastInstr).getLabelFalse();
            basicBlock.addNextBlock(targetLabel1);
            basicBlock.addNextBlock(targetLabel2);
            label2Block.get(targetLabel1).addPrevBlock(basicBlock.getLabel());
            label2Block.get(targetLabel2).addPrevBlock(basicBlock.getLabel());
        } else { //TODO:return 语句
            if (nextBlock != null) {
                basicBlock.addNextBlock(nextBlock.getLabel());
                nextBlock.addPrevBlock(basicBlock.getLabel());
            } else {
                basicBlock.addNextBlock(currentFunc.getFuncEntry().name() + "_exit");
            }
        }
    }

    public static IrModule removeEmptyBlock(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            label2Block = funcDef.getLabel2Block();
            Iterator<BasicBlock> iterator = funcDef.getBasicBlocks().iterator();
            while (iterator.hasNext()) {
                BasicBlock basicBlock = iterator.next();
                if (basicBlock.isEmpty() && !basicBlock.equals(funcDef.getExitBlock())) {
                    String emptyLabel = basicBlock.getLabel();
                    HashSet<String> prevBlocks = basicBlock.getPrevBlock();
                    String toBlockLabel = basicBlock.getNextBlock().iterator().next();
                    for (String preLabel : prevBlocks) {
                        BasicBlock prevBlock = label2Block.get(preLabel);
                        if (prevBlock != null) {
                            prevBlock.getNextBlock().remove(emptyLabel);
                            prevBlock.getNextBlock().add(toBlockLabel);
                            if (prevBlock.getLastInstruction() instanceof Jump) {
                                ((Jump) prevBlock.getLastInstruction()).setTarget(toBlockLabel);
                            } else if (prevBlock.getLastInstruction() instanceof Branch) {
                                ((Branch) prevBlock.getLastInstruction()).replaceTarget(emptyLabel, toBlockLabel);
                            }
                        }
                    }
                    BasicBlock toBlock = label2Block.get(toBlockLabel);
                    if (toBlock != null) {
                        toBlock.getPrevBlock().remove(emptyLabel);
                        toBlock.getPrevBlock().addAll(prevBlocks);
                    }
                    if (DBG_MODE) {
                        System.out.println("remove empty:" + emptyLabel);
                    }
                    iterator.remove();
                }
            }
        }

        return irModule;
    }

    public static IrModule mergeBlock(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            Map<String, BasicBlock> label2block = funcDef.getLabel2Block();
            boolean changed;
            do {
                changed = false;
                Iterator<BasicBlock> iterator = funcDef.getBasicBlocks().iterator();
                BasicBlock it;
                while (iterator.hasNext()) {
                    it = iterator.next();
                    if (!it.equals(funcDef.getExitBlock())) {
                        if (it.getPrevBlock().size() == 1 &&
                                !it.getPrevBlock().iterator().next().equals(entryLabel)) {
                            BasicBlock preBlock = label2block.get(it.getPrevBlock().iterator().next());
                            if (!(preBlock.getLastInstruction() instanceof Branch)) {
                                // 合并
                                //去掉前一个基本快的最后的跳转
                                if (preBlock.getLastInstruction() instanceof Jump) {
                                    preBlock.removeInstr(preBlock.getLastInstruction());
                                }
                                // 指令拼接
                                preBlock.getLastInstruction().setNext(it.getFirstInstruction());
                                it.getFirstInstruction().setPrev(preBlock.getLastInstruction());
                                preBlock.getEnd().setPrev(it.getLastInstruction());
                                it.getLastInstruction().setNext(preBlock.getEnd());
                                // 改流图
                                preBlock.getNextBlock().remove(it.getLabel());
                                preBlock.getNextBlock().addAll(it.getNextBlock());
                                // 补跳转
                                if (!(it.getLastInstruction() instanceof Jump ||
                                        it.getLastInstruction() instanceof Branch)) {
                                    if (!it.getNextBlock().iterator().next().equals(funcDef.getExitBlock().getLabel())) {
                                        preBlock.addAfter(new Jump(it.getNextBlock().iterator().next()));
                                    }
                                }
                                for (String nextBlockLabel : it.getNextBlock()) {
                                    BasicBlock nextBlock = label2block.get(nextBlockLabel);
                                    nextBlock.getPrevBlock().remove(it.getLabel());
                                    nextBlock.getPrevBlock().add(preBlock.getLabel());
                                }
                                // 删基本快
                                label2block.remove(it.getLabel());
                                iterator.remove();
                                changed = true;
                            }
                        }
                    }
                }
            } while (changed);
        }
        return irModule;
    }

    public static IrModule removeUnreachable(IrModule irModule) {
        for (FuncDef funcDef : irModule.getFuncDefs()) {
            Map<String, BasicBlock> label2block = funcDef.getLabel2Block();
            boolean changed = false;
            do {
                changed = false;
                Iterator<BasicBlock> iterator = funcDef.getBasicBlocks().iterator();
                while (iterator.hasNext()) {
                    BasicBlock it = iterator.next();
                    if (!it.equals(funcDef.getExitBlock())) {
                        if (it.getPrevBlock().size() == 0) {
                            for (String labelNext : it.getNextBlock()) {
                                label2block.get(labelNext).getPrevBlock().remove(it.getLabel());
                            }
                            if (DBG_MODE) {
                                System.out.println("delete:" + it.getLabel());
                            }
                            label2block.remove(it.getLabel());
                            iterator.remove();
                            changed = true;
                        }
                    }
                }

            } while (changed);
        }
        return irModule;
    }


}
