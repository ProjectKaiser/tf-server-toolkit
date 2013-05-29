/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

public class EDSException extends RuntimeException {
	private static final long serialVersionUID = -3578258056622236748L;
	
	public EDSException(String msg) {
		super(msg);
	}
	
	public static class ECVRColumnException extends EDSException{
		private static final long serialVersionUID = -2743104309087143968L;
		private String m_column;
		
		public ECVRColumnException(String name){
			super(name);
			m_column = name;
		}
		public String getColumn(){
			return m_column;
		}
		
		public static class EColumnNotFound extends ECVRColumnException{
			private static final long serialVersionUID = -1814875739324435961L;
			public EColumnNotFound(String name) {
				super(name);
			}
			public static class EWrongColumnName extends EColumnNotFound{
				private static final long serialVersionUID = -8208418178174584919L;
		
				EWrongColumnName(String name){
					super(name);
		    	}	
		    }
		    
		    public static class EWrongFieldFunctionName extends EColumnNotFound{
				private static final long serialVersionUID = 7897362791302709165L;
		
				EWrongFieldFunctionName(String name){
					super(name);
		    	}	
		    }
		    public static class EWrongNameInWhereClause extends EColumnNotFound{
				private static final long serialVersionUID = -7053990642326330353L;
		
				EWrongNameInWhereClause(String name){
					super(name);
		    	}	
		    }
		    public static class EWrongNameInOrderClause extends EColumnNotFound{
				private static final long serialVersionUID = -3481793997998777240L;
		
				EWrongNameInOrderClause(String name){
					super(name);
		    	}	
		    }
		}
		
		public static class EColumnRequestedTwice extends ECVRColumnException{
			private static final long serialVersionUID = -7035253412958282181L;
	
			EColumnRequestedTwice(String name){
	    		super(name);
	    	}	
	    }
	    
	}

	public static class EDSProviderNotFound extends EDSException{
		private static final long serialVersionUID = 2735948286065733152L;
		public EDSProviderNotFound(String target) {
			super(target);
		}
		
	} 
}
