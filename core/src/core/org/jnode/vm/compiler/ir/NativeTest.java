/**
 * This file is part of the ALLDB project
 * Author: Levente S?ntha
 * Created on Apr 18, 2004, 6:59:40 PM  
 */
package org.jnode.vm.compiler.ir;

import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.compiler.l2.X86CodeGenerator;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.assembler.x86.TextX86Stream;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.util.BootableArrayList;
import org.jnode.util.BootableHashMap;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;

/**
 * @author Levente S?ntha
 */
public class NativeTest {
//    static  {
//        System.loadLibrary("exec");
//    }

//    private static native int callNative(int a0, int a1, byte[] code, int size);

    public static void main(String args[]) throws SecurityException, IOException, ClassNotFoundException {
//        System.in.read();
            X86CpuID cpuId = X86CpuID.createID("p5");
            boolean binary = false;

            String className = "org.jnode.vm.compiler.ir.PrimitiveTest";
            if (args.length > 0) {
                String arg0 = args[0];
                if("-b".equals(arg0)){
                    binary = true;
                    if(args.length > 1){
                        className = args[1];
                    }
                }else{
                    className = arg0;
                }
            }

            if(binary){
                X86Stream os = new X86Stream(cpuId, 0);
                generateCode(os, className);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                os.writeTo(baos);
                baos.close();
                byte[] b = baos.toByteArray();

                FileOutputStream fos = new FileOutputStream("test.bin");
                fos.write(b);
                fos.close();

                /*
                int[] icode = new int[b.length];
                for(int i=0; i< b.length; i++){
                    icode[i] = b[i];
                }
                for(int i=0; i< b.length; i++){
                    System.out.println("J: " + icode[i]);
                }*/

                //System.out.println("result: " + callNative(5, 3, b, b.length));

            }else{
                TextX86Stream tos = new TextX86Stream(new OutputStreamWriter(System.out), cpuId);
                generateCode(tos, className);
                tos.flush();
            }



/*
		BytecodeViewer bv = new BytecodeViewer();
		BytecodeParser.parse(code, bv);

		// System.out.println(cfg.toString());
		// System.out.println();

		boolean printDeadCode = false;
		boolean printDetail = false;
		IRBasicBlock currentBlock = null;
		for (int i=0; i<n; i+=1) {
			Quad quad = (Quad) quads.get(i);
			if (currentBlock != quad.getBasicBlock()) {
				currentBlock = quad.getBasicBlock();
				System.out.println();
				System.out.println(currentBlock);
			}
			if (printDeadCode && quad.isDeadCode()) {
				if (printDetail) {
					printQuadDetail(quad);
				}
				System.out.println(quad);
			}
			if (!quad.isDeadCode()) {
				if (printDetail) {
					printQuadDetail(quad);
				}
				System.out.println(quad);
			}
		}

		System.out.println();
		System.out.println("Live ranges:");
		n = lv.size();
		for (int i=0; i<n; i+=1) {
			System.out.println(liveRanges[i]);
		}
*/
        }

        private static void generateCode(AbstractX86Stream os, String className) throws MalformedURLException, ClassNotFoundException {


            VmByteCode code = loadByteCode(className);

            X86CodeGenerator x86cg = new X86CodeGenerator(os, code.getLength());

            IRControlFlowGraph cfg = new IRControlFlowGraph(code);

            //BytecodeViewer bv = new BytecodeViewer();
            //BytecodeParser.parse(code, bv);

            //System.out.println(cfg.toString());
            //System.out.println();

            System.out.println(cfg);
            IRGenerator irg = new IRGenerator(cfg);
            BytecodeParser.parse(code, irg);

            BootableArrayList quads = irg.getQuadList();
            int n = quads.size();
            BootableHashMap liveVariables = new BootableHashMap();
            for (int i=0; i<n; i+=1) {
                Quad quad = (Quad) quads.get(i);
//            System.out.println(quad);
                quad.doPass2(liveVariables);
                System.out.println(quad);
            }

            Collection lv = liveVariables.values();
            n = lv.size();
            LiveRange[] liveRanges = new LiveRange[n];
            Iterator it = lv.iterator();
            for (int i=0; i<n; i+=1) {
                Variable v = (Variable) it.next();
                liveRanges[i] = new LiveRange(v);
                // System.out.println("Live range: " + liveRanges[i]);
            }
            Arrays.sort(liveRanges);
            System.out.println(Arrays.asList(liveRanges));
            LinearScanAllocator lsa = new LinearScanAllocator(liveRanges);
            lsa.allocate();

            x86cg.setArgumentVariables(irg.getVariables(), irg.getNoArgs());
            x86cg.setSpilledVariables(lsa.getSpilledVariables());
            x86cg.emitHeader();

            n = quads.size();
            for (int i=0; i<n; i+=1) {
                Quad quad = (Quad) quads.get(i);
                if (!quad.isDeadCode()) {
                    quad.generateCode(x86cg);
                }
            }
        }

        private static VmByteCode loadByteCode(String className)
            throws MalformedURLException, ClassNotFoundException {
            VmSystemClassLoader vmc = new VmSystemClassLoader(new File(".").toURL(), new VmX86Architecture());
            VmType type = vmc.loadClass(className, true);
            VmMethod arithMethod = null;
            int nMethods = type.getNoDeclaredMethods();
            for (int i=0; i<nMethods; i+=1) {
                VmMethod method = type.getDeclaredMethod(i);
                if ("terniary".equals(method.getName())) {
                    arithMethod = method;
                    break;
                }
            }
            VmByteCode code = arithMethod.getBytecode();
            return code;
        }

        public static void printQuadDetail(Quad quad) {
            System.out.print(quad.getBasicBlock());
            System.out.print(" ");
            Variable[] vars = quad.getBasicBlock().getVariables();
            System.out.print("[");
            for (int j=0; j<vars.length; j+=1) {
                System.out.print(vars[j]);
                System.out.print(",");
            }
            System.out.print("] ");
            if (quad.isDeadCode()) {
                System.out.print("(dead) ");
            }
        }

        public static int arithOptLoop(int a0, int a1, int a2) {
            int l3 = 1;
            int l4 = 3*a1;
            for (int l5=10; l5 > 0; l5-=1) {
                l3 += 2*a0 + l4;
                l4 += 1;
            }
            return l3;
        }

        public static int arithOptIntx(int a0, int a1, int a2) {
            return a0 + a1;
        }

        public static int discriminant(int a0, int a1, int a2) {
            return a1*a1 - 4*a0*a2;
        }

        public static int simple(int a0, int a1) {
            int l0 = a1;
            return -l0;
        }

        public static int const0(int a0, int a1) {
            int l0 = 0;
            if (a0 == 0) {
                l0 = -1;
            }
            if (a0 > 0) {
                l0 = 1;
            }
            return l0;
        }

        public static int const12(int a0, int a1) {
            int l0 = 10, l1 = 0;
            while(l0 > 0){
                l1 = a1 * l1 + a0;
                l0 = l0 - 1;
            }
            return l1;
        }

        //compile it with no optimisation (see kjc -O0 - the kopi compiler)
        public static int unconditionalJump(int a0, int a1) {
            int l0 = 1;
            for(;;){
                l0 = a0 + a1 + l0;
                for(;;){
                    l0 = a0 + a1 + l0;
                    for(;;){
                        l0 = a0 + a1 + l0;
                        break;
                    }
                    break;
                }
                break;
            }
            return l0;
        }

        public static int const6(int a0, int a1) {
            int l1 = a1 | a0;
            int l2 = a0 & a1;
            return l1 ^ l1 + l2 ^ l2 - 2  * l1 * l2;
        }

        void const5() {
            int l1 = 0, l2 = 1, l3 = 3;
            l3 = l1 + l2;
        }

        public static int const4(int a0, int a1) {
            int l1 = a1 + a0;
            int l2 = a0 * a1;
            return l1 * l1 + l2 * l2 + 2  * l1 * l2;
        }

        public static int const3(int a0, int a1) {
            int l1 = -134;
            int l2 = 2;
            int l3 = 3;
            //return (int)(1 + 2 * 3.5 + 1) % 6  ;
            //return  - ((l1 + l2 + l3 + l1* l2* l3) / l2);
            return a1 + a1 + a0;
        }

        public static int const2(int a0, int a1) {
            int l1 = -134;
            int l2 = 2;
            int l3 = 3;
            //return (int)(1 + 2 * 3.5 + 1) % 6  ;
            //return  - ((l1 + l2 + l3 + l1* l2* l3) / l2);
            return (byte) 2 + a1;
        }

        public static int erro1(int a0, int a1) {
            return a0 < a1 ? a0 : a1;
        }

        public static int ifTest(int a0, int a1) {
            int l0;
            if(a0 > a1)
                l0 = a0;
            else
                l0 = a1;
            return l0;
        }

        public static int const1(int a0, int a1) {
            int l0 = 1;
            for(;;){
                l0 = a0 + a1 + l0;
                if(l0 > 10)
                    break;
            }
            return l0;
        }

        public static int terniary(int a0, int a1) {
            return 0;
        }

        /*
        public static int terniary(int a0, int a1) {
            return 1;
        }

        /*
        public static int terniary(int a0, int a1) {
            return a0 < 0 ? a0 : a1;
        }

        /*
        public static int terniary(int a0, int a1) {
            int l0 = a0 + a0;
            int l1 = a1 * a1;
            return l0 < l1 ? a0 : a1;
        }


        public static int const4(int a0, int a1) {
            int l1 = a1 + a0;
            int l2 = a0 * a1;
            return l1 * l1 + l2 * l2 + 2  * l1 * l2;
        }*/
}
