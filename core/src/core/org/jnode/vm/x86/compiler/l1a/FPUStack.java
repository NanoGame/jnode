/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;
import org.jnode.vm.bytecode.StackException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPUStack extends ItemStack {

    private static final Register[] REGS = { Register.ST0, Register.ST1,
            Register.ST2, Register.ST3, Register.ST4, Register.ST5,
            Register.ST6, Register.ST7};

    /**
     * Initialize this instance.
     */
    FPUStack() {
        super(Item.Kind.FPUSTACK, REGS.length);
    }

    /**
     * Gets the FPU register that contains the given item.
     * 
     * @see org.jnode.assembler.x86.Register#ST1
     * @param item
     * @return
     */
    final Register getRegister(Item item) {
        for (int i = 0; i < tos; i++) {
            if (stack[ tos - (i + 1)] == item) { return REGS[ i]; }
        }
        throw new StackException("Item not found on FPU stack");
    }
    
    /**
     * Gets the item that is contained in the given register.
     */
    final Item getItem(Register fpuReg) {
        final int idx = tos-(fpuReg.getNr() + 1);
        return stack[idx];
    }
    
    /**
     * Swap the top of the stack (ST0) with the given FPU reg.
     * @param fpuReg
     */
    final void fxch(Register fpuReg) {
        final int idx1 = tos-1;
        final int idx2 = tos-(fpuReg.getNr() + 1);
        final Item tmp = stack[idx1];
        stack[idx1] = stack[idx2];
        stack[idx2] = tmp;
    }
}