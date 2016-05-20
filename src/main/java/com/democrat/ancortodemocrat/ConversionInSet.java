package com.democrat.ancortodemocrat;

import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.EmbeddedUnit;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Unit;


/**
 * Add element REF to each relation/mention from first mention
 * or chain type
 * @param annotation Annotation 
 */
public class ConversionInSet implements Runnable{
	
	
	private static Logger logger = Logger.getLogger(ConversionInSet.class);


	public static void toSetFromFirstMention( Annotation annotation ){

		
		List<Unit> unitList = annotation.getUnit();
		//.addAll( annotation.getSchema() );
		int currentRef = 0;

		for(Unit unit : unitList){
			if( unit.isNew( annotation ) && ! unit.isContainedInSchema( annotation ) ){
				setRefFeatureFromFirstMention( annotation, unit, currentRef );
				currentRef++;
			}
		}
		
		
		
		List<Schema> schemaList = annotation.getSchema();
		//same thing with schema, but there are not added in unit list
		//because, during the xml generation, the dtd will broke
		for(Schema schema : schemaList){
			if( schema.isNew( annotation ) ){
				setRefFeatureFromFirstMention( annotation, schema, currentRef );
				currentRef++;
			}
		}
		

	}
	
	private static void setRefFeatureFromFirstMention(Annotation annotation , Unit unit, int currentRef){
		List<Relation> relationList = annotation.getRelationContaining( unit );
		unit.setFeature( "REF" , currentRef + "");
		for(Relation relation : relationList){
			Unit element = (Unit) relation.getElement( annotation );
			if( element == null ){
				//relation --> relation 
				//mistake from annotation
				continue;
			}
			relation.setFeature( "REF" , currentRef + "" );
			element.setFeature( "REF", currentRef + "" );
		}
		//if it's a schema, set the ref on any unit in this schema
		if(unit instanceof Schema){
			List<EmbeddedUnit> embeddedUnitList = unit.getPositioning().getEmbeddedUnit();
			for(int e = 0; e < embeddedUnitList.size(); e++){
				embeddedUnitList.get( e ).getElement( annotation ).setFeature( "REF" , currentRef + "");
			}
		}

	}

	public static void toSetFromChain( Annotation annotation ){

		List<Unit> unitList = annotation.getUnit();
		//unitList.addAll( annotation.getSchema() );
		int currentRef = 0;

		for(Unit unit : unitList){
			if( unit.isNew( annotation ) && ! unit.getId().contains( "TXT_IMPORTER") &&
					! unit.isContainedInSchema( annotation ) ){
				//starting set new REF on each ref and unit associated
				boolean done = false;
				Relation lastRelation = null;
				Unit currentUnit = unit;
				while( !done ){
					//while there is again an other unit/schema after
					// (first unit/schema) | <-- | <-- | <-- (| last unit/schema)
					List<Relation> relationAssociated = annotation.getRelationContaining( currentUnit );
					logger.debug( relationAssociated.size() + " unit " + currentUnit);
					if( relationAssociated.size() == 1){
						//last or first unit
						//if last stop it
						if( ! relationAssociated.get( 0 ).containsUnit(annotation, unit) ){
							//if the relation doesnt contains the first unit (NEW)
							currentUnit.setFeature("REF", currentRef+"");
							done = true;
						}else{
							//first
							setRefFeatureFromChain(annotation, relationAssociated.get( 0 ), currentRef);
						}
						lastRelation = relationAssociated.get( 0 );
						currentUnit = (Unit) relationAssociated.get( 0 ).getOtherElement(annotation, currentUnit);
					}else if(relationAssociated.size() == 2){
						//between two units/schemas
						if( relationAssociated.get( 0 ).equals( lastRelation )){
							setRefFeatureFromChain(annotation, relationAssociated.get( 1 ), currentRef);

							lastRelation = relationAssociated.get( 1 );
							currentUnit = (Unit) relationAssociated.get( 1 ).getOtherElement(annotation, currentUnit);
						}else{
							//0
							setRefFeatureFromChain(annotation, relationAssociated.get( 0 ), currentRef);	

							lastRelation = relationAssociated.get( 0 );
							currentUnit = (Unit) relationAssociated.get( 0 ).getOtherElement(annotation, currentUnit);
						}
					}	
				}
				currentRef++;
			}
		}
	}
	
	/**
	 * set the ref on the relation and on the unit/schema of the relation
	 */
	private static void setRefFeatureFromChain( Annotation annotation, Relation relation, int ref ){
		relation.setFeature("REF", ref+"");
		relation.getPositioning().getTerm().get( 0 ).getElement( annotation ).setFeature( "REF", ref + "");
		relation.getPositioning().getTerm().get( 1 ).getElement( annotation ).setFeature( "REF", ref + "");
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
