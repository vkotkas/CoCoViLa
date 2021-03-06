package ee.ioc.cs.vsle.util;

import ee.ioc.cs.vsle.ccl.CompileException;
import ee.ioc.cs.vsle.editor.RuntimeProperties;

/**
 * <p>Title: DebugPrinter</p>
 * <p>Description: Debug information printer. Central debug information printing
 * utility to allow filtering of printed information etc.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Ando Saabas
 * @version 1.0
 */
public class db {

    /**
     * Print out any object.
     * @param o Object - object to be printed.
     */
    public static void p(Object o) {
        System.out.println(o);
    } // p

    /**
     * Prints the compilation error message to System.err stream.
     * When debugging is switched on the stack trace is also printed.
     * @param e CompileException
     */
    public static void p(CompileException e) {
        System.err.println(e.toString());
        if (RuntimeProperties.isLogDebugEnabled())
            e.printStackTrace();
    }

    /**
     * Prints the stack trace to System.err stream. 
     * @param e exception
     */
    public static void p(Throwable e) {
        e.printStackTrace();
    }

    /**
     * Print out boolean values.
     * @param b boolean - boolean value to be printed.
     */
    public static void p(boolean b) {
        System.out.println(b);
    } // p

    /**
     * Print out integer values.
     * @param i int - integer value to be printed.
     */
    public static void p(int i) {
        System.out.println(i);
    } // p

    /**
     * Print out float values.
     * @param f float - float value to be printed.
     */
    public static void p(float f) {
        System.out.println(f);
    } // p

    public static void p(double f) {
        System.out.println(f);
    } // p
}
