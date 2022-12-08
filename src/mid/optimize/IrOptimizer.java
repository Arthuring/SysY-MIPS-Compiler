package mid.optimize;

import mid.IrModule;

public class IrOptimizer {
    public static IrModule optimize(IrModule irModule) {
        irModule = FlowGraphBuilder.buildFlowGraph(irModule);
        irModule = FlowGraphBuilder.removeEmptyBlock(irModule);
        irModule = FlowGraphBuilder.mergeBlock(irModule);
        irModule = Peephole.optimize(irModule);
        irModule = ConstantPropagation.constPro(irModule);
        irModule = DeadCodeKiller.deadCodeInBasicBlock(irModule);
        irModule = DeadCodeKiller.killUnreachableBlock(irModule);
        irModule = CopyPropagation.copyPropagation(irModule);
        irModule = DeadCodeKiller.deadCodeInBasicBlock(irModule);
        irModule = ColorScheduler.colorScheduleReg(irModule);
        return irModule;
    }
}
