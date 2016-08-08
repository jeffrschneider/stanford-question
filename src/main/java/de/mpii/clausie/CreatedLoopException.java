/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpii.clausie;

/**
 *
 * @author grv
 */
public class CreatedLoopException extends Exception{

	public CreatedLoopException() {
		super("Clausie tried to create a loop in the dependency tree, "
				+ "this sentence will not be parsed");
	}
	
}
