package org.um.feri.analyse.attractionBasins;

import java.io.Serializable;

//Our point in search space with its coordinates, fitness value and id of attraction basin
public class Point implements Serializable{
	private static final long serialVersionUID = 7526473423423423149L;  // unique id - for serialization
	public double x1;
	public double x2;
	public double f;
	public int color;
	public int plateau;
}
