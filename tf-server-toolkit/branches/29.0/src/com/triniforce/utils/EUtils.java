/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

import java.text.MessageFormat;

public class EUtils {
	
    public static class ENotSupportedPrefix extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ENotSupportedPrefix(String name) {
            super(MessageFormat.format("Unsupported string serialization prefix: {0}", name));
        }
    }
    
    public static class EAssertionFailed  extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public EAssertionFailed(String msg) {
			super(msg);
		}
    }
    
    public static class EAssertTrueFailed extends EAssertionFailed {
		private static final long serialVersionUID = 1L;
		public EAssertTrueFailed(String msg) {
            super(msg);
        }
    }
    public static class EAssertEqualsFailed extends EAssertionFailed {
        private static final long serialVersionUID = 1L;
        public EAssertEqualsFailed(Object expected, Object actual) {
            super(MessageFormat.format("Objects are not equals {0}, {1}", expected, actual)); //$NON-NLS-1$);
        }
    }
    public static class EAssertNotNullFailed extends EAssertionFailed {
        private static final long serialVersionUID = 1L;
        public EAssertNotNullFailed(String msg) {
            super(msg);
        }
    }    
	
    public static class EParentNotExist extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public EParentNotExist(String msg) {
            super(msg);
        }
    }	

   
    public static class EResourceNotFound extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EResourceNotFound(Class cls, String resourceName) {
            super(MessageFormat.format(Messages.getString("EUtils.0"), resourceName, cls)); //$NON-NLS-1$
        }
    }
    
}
