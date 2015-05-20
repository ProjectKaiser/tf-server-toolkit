/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.extensions;

import java.text.MessageFormat;

@SuppressWarnings("serial")
public class EIncompatibleExtensionClass extends RuntimeException {

	public EIncompatibleExtensionClass(String epId, Class mustBe, String eId,
			Class current) {
		super(
				MessageFormat
						.format(
								"Extension point {0} should have extensions of type {1}, whereas extension {2} is of type {3}",
								epId, mustBe.getName(), eId, current.getName()));
	}
}
