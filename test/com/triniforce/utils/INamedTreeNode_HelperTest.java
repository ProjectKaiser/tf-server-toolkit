/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

import com.triniforce.utils.INamedTreeNode.NamedTreeNode;

import junit.framework.TestCase;

public class INamedTreeNode_HelperTest extends TestCase {

	public void testGetPath() {
		{
			NamedTreeNode n1 = new NamedTreeNode();
			n1.setLocalTreeNodeName("n1");
			assertEquals("/n1", n1.getTreePath());

			NamedTreeNode n11 = new NamedTreeNode();
			n11.setLocalTreeNodeName("n11");
			n11.setParent(n1);
			assertEquals("/n1/n11", n11.getTreePath());

			NamedTreeNode n12 = new NamedTreeNode();
			n12.setLocalTreeNodeName("n12");
			n12.setParent(n1);
			assertEquals("/n1/n12", n12.getTreePath());

			NamedTreeNode n121 = new NamedTreeNode();
			n121.setLocalTreeNodeName("n121");
			n121.setParent(n12);
			assertEquals("/n1/n12/n121", n121.getTreePath());

			NamedTreeNode n122 = new NamedTreeNode();
			n122.setLocalTreeNodeName("n122");
			n122.setParent(n12);
			assertEquals("/n1/n12/n122", n122.getTreePath());
			
			n1.setLocalTreeNodeName(null);
			assertEquals("/null/n12/n122", n122.getTreePath());
			n12.setLocalTreeNodeName(null);
			assertEquals("/null/null/n122", n122.getTreePath());
			n12.setParent(null);
			assertEquals("/null/n122", n122.getTreePath());
			
		}

	}

}
