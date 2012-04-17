/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

public class Err {

    public static class EWhereClauseNotSupported extends RuntimeException {
        private static final long serialVersionUID = -5641247006795770188L;
        public EWhereClauseNotSupported(String msg) {
            super(msg);
        }
    }
    
    public static class ENotAllowedExprType extends RuntimeException {
        private static final long serialVersionUID = -8805168989521415317L;
        public ENotAllowedExprType(String msg) {
            super(msg);
        }
    }
    
    public static class EColNotFound extends RuntimeException {
        private static final long serialVersionUID = -5251578070181360837L;
        public EColNotFound(String msg) {
            super(msg);
        }
    }

    public static class EColAlreadyExists extends RuntimeException {
        private static final long serialVersionUID = 4812627431522262587L;
        public EColAlreadyExists(String msg) {
            super(msg);
        }
    }

    public static class EPrefixAlreadyExists extends RuntimeException {
        private static final long serialVersionUID = 8391770157727551324L;
    
        public EPrefixAlreadyExists(String msg) {
            super(msg);
        }
    }

    public static class EPrefixNotFound extends RuntimeException {
        private static final long serialVersionUID = -189697723455556725L;
    
        public EPrefixNotFound(String msg) {
            super(msg);
        }
    }

}
