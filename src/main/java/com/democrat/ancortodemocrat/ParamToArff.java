package com.democrat.ancortodemocrat;

/**
 * paramètres possible pour la commande arff
 * pour choisir quel type de relation prendre
 * @author buggr
 *
 */
public enum ParamToArff {
	
	ALL("all"),
	NO_ASSOC("no_assoc");
	
	private String param;

	ParamToArff(String param){
		this.param = param;
	}

	
	ParamToArff toEnum(String param){
		if(param.equalsIgnoreCase( "no_assoc" ) ){
			return ParamToArff.NO_ASSOC;
		}else{
			return ParamToArff.ALL;
		}
	}
	
}