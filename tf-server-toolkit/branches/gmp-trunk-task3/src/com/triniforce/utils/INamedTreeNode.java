/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

public interface INamedTreeNode {
	public static class Helper {
		public static String getPath(INamedTreeNode parent,
				INamedTreeNode local) {
			String res = null;
			if (null == parent) {
				res = "/";//$NON-NLS-1$
			}else{
				res = parent.getTreePath()+"/";//$NON-NLS-1$
			}
			res = res + local.getLocalTreeNodeName();
			return res;
		}
	}
	public static class NamedTreeNode implements INamedTreeNode{

		protected String m_localName;
		protected INamedTreeNode m_parent;
		
		public INamedTreeNode getParent() {
			return m_parent;
		}

		public void setParent(INamedTreeNode parent) {
			m_parent = parent;
		}

		public String getLocalTreeNodeName() {
			return m_localName;
		}

		public String getTreePath() {
			return Helper.getPath(getParent(), this);
		}

		public void setLocalTreeNodeName(String localName) {
			m_localName = localName;
		}
		@Override
		public String toString() {
			return getTreePath();
		}
	}
	String getLocalTreeNodeName();
	String getTreePath();
}
