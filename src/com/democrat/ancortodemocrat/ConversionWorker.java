package com.democrat.ancortodemocrat;

import java.util.ArrayList;
import java.util.List;

import javax.management.relation.RelationService;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Cluster;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Feature;
import com.democrat.ancortodemocrat.element.PositioningRelation;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Type;
import com.democrat.ancortodemocrat.element.Unit;

public class ConversionWorker {

	private static Logger logger = Logger.getLogger(ConversionWorker.class);

	private Corpus corpus;

	public ConversionWorker( Corpus corpus ){
		this.corpus = corpus;
	}

	public void work(){
		logger.info("Start converting: " + corpus.getName() );
		
		for( int a = 0; a < this.corpus.getAnnotation().size(); a++ ){

			logger.info("--> Converting file: "+(a + 1)+"/"+this.corpus.getAnnotation().size() + " : " + this.corpus.getAnnotation().get( a ).getFileName() );
			Annotation annotation = this.corpus.getAnnotation().get(a);
			this.convertRelationToChain( annotation );
			this.convertFeature( annotation );
		}
	}

	/**
	 * Take a list of relation annoted in first mention, 
	 * and convert it in chain
	 * @param relation
	 */
	private void convertRelationToChain( Annotation annotation ){
		List<Relation> newRelations = new ArrayList<Relation>();
		List<Relation> relations = annotation.getRelation();

		for(int r = 0; r < relations.size(); r++){
			Relation relation = relations.get( r );
			Relation newRelation = Relation.newInstance( relation );

			PositioningRelation positioning = relation.getPositioning();

			if( positioning != null){

				if(positioning.getTerm().size() > 1){
					//we are checking the two pointers in the current relation
					//if one point on the first element (new = yes) 
					//so replace this point with the unit the more closer before
					//so the unit in fist mention of the pre relation of this unit
					Relation preRelation = relation.getPreRelation( annotation );
					
					if( preRelation == null){
						//first relation
						newRelations.add( newRelation );
						continue;
					}
					Element element = preRelation.getElement( annotation );
					if(element instanceof Unit){
						Element termElement = positioning.getTerm().get( 0 ).getElement( annotation );
						if(termElement instanceof Unit){
							if( ((Unit) termElement).isNew( annotation ) ){
								newRelation.getPositioning().getTerm().get( 0 ).setId( element.getId()  );
							}
						}if(positioning.getTerm().get( 1 ).getElement( annotation ) instanceof Unit){
							if( ((Unit) positioning.getTerm().get( 1 ).getElement( annotation )).isNew( annotation ) ){
								newRelation.getPositioning().getTerm().get( 1 ).setId( element.getId()  );
							}
						}
					}else{
						//TODO relation to relation
						logger.debug("Relation to relation..");
					}

				}
			}

			newRelations.add( newRelation );
		}

		annotation.setRelation( newRelations );
	}

	/**
	 * recalculates the characterisation(type) and features of the relation
	 * <ul>
	 * <li>NB</li>
	 * <li>GENRE</li>
	 * </ul>
	 * in comparaison with the preRelation not converted
	 * @param relations
	 */
	private void convertCharacterisation( Annotation annotation, Relation relation, Relation preRelation ){
		String currentType = relation.getCharacterisation().getType().getValue();
		String preType = preRelation.getCharacterisation().getType().getValue();
		
		if(currentType.equalsIgnoreCase( "DIRECTE" ) && preType.equalsIgnoreCase( "INDIRECTE" )){
			//(NO, DIR, IND) --> DIR
			relation.getCharacterisation().setType( new Type("INDIRECTE") );
		}else if(currentType.equalsIgnoreCase("DIR") && preType.equalsIgnoreCase( "PR" )){
			//(NO, DIR, PR) --> IND
		}
		
		
		
		//type
		//nb
		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if(element instanceof Unit && preElement instanceof Unit){
			Unit unit = (Unit) element;
			Unit preUnit = (Unit) preElement;
			String currentNb = unit.getFeature( "NB" );
			String preNb = unit.getFeature( "NB" );


			String currentGenre = unit.getFeature( "GENRE" );
			String preGenre = unit.getFeature( "GENRE" );
			
			
		}
		
	}
	
	private void convertCharacterisation( Annotation annotation, Relation relation){
		this.convertFeature( annotation, relation, relation.getPreRelation( annotation ));
	}


}