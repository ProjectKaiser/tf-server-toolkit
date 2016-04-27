package com.triniforce.server.soap;

public class VNone {

	public static final VNone none = new VNone();
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof VNone;
	}
	
	@Override
	public String toString() {
		return "VNone";
	}

}
