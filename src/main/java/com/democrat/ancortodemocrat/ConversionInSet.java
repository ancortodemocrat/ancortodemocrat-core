package com.democrat.ancortodemocrat;

import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
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
			setRefOnUnit( annotation, (Schema) unit, currentRef);
		}

	}

	public static void toSetFromChain( Annotation annotation ){

		List<Unit> unitList = annotation.getUnit();
		int currentRef = 0;

		for(Unit unit : unitList){
			if( unit.isNew( annotation ) && ! unit.getId().contains( "TXT_IMPORTER") &&
					! unit.isContainedInSchema( annotation ) ){
				//starting set new REF on each ref and unit associated
				treatUnitFromChain( annotation, unit, currentRef, null );
				currentRef++;
			}
		}
		List<Schema> schemaList = annotation.getSchema();
		for(Schema schema : schemaList){
			if( schema.isNew( annotation ) && ! schema.getId().contains( "TXT_IMPORTER") &&
					! schema.isContainedInSchema( annotation ) ){
				//starting set new REF on each ref and unit associated
				treatUnitFromChain( annotation, schema, currentRef, null );
				currentRef++;
			}
		}
	}


	/**
	 * recursive method
	 * @param annotation
	 * @param unit
	 * @param currentRef id of the ref who will be set
	 * @param lastRelation for the first call, set to null
	 */
	private static void treatUnitFromChain( Annotation annotation, Unit unit, int currentRef, Relation lastRelation){

		if(unit instanceof Schema){
			setRefOnUnit( annotation, (Schema) unit, currentRef);
		}
		List<Relation> relationAssociated = annotation.getRelationContaining( unit );

		if( relationAssociated.size() == 0){
			return;
		}

		if( relationAssociated.size() == 1){
			//last or first unit
			//if last stop it
			if( ! relationAssociated.get( 0 ).containsUnit(annotation, unit)){
				//if the relation doesnt contains the first unit (NEW)
				unit.setFeature("REF", currentRef+"");
				return;
			}else{
				//first
				setRefFeatureFromChain(annotation, relationAssociated.get( 0 ), currentRef);

			}
			lastRelation = relationAssociated.get( 0 );
			//currentUnit = (Unit) relationAssociated.get( 0 ).getOtherElement(annotation, currentUnit);

			//it means that the chain contains only one relation, so with two units/schemas
			//not need to go further
			if( annotation.getRelationContaining( unit ).size() == 1 ){
				return;
			}
		}else if(relationAssociated.size() == 2){
			
			if(lastRelation != null){
				if( relationAssociated.get( 0 ).equals( lastRelation )){
					setRefFeatureFromChain(annotation, relationAssociated.get( 1 ), currentRef);
					treatUnitFromChain( annotation, (Unit) relationAssociated.get( 1 ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( 1 ) );
				}else{
					//0
					setRefFeatureFromChain(annotation, relationAssociated.get( 0 ), currentRef);
					treatUnitFromChain( annotation, (Unit) relationAssociated.get( 0 ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( 0 ) );
				}
			}else{
				//catahore case
				treatUnitFromChain( annotation, (Unit) relationAssociated.get( 0 ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( 0 ) );
				treatUnitFromChain( annotation, (Unit) relationAssociated.get( 1 ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( 1 ) );
			}
		}

	}

	/**
	 * set the ref on the relation and on the unit/schema of the relation
	 */
	private static void setRefFeatureFromChain( Annotation annotation, Relation relation, int ref ){
		relation.setFeature("REF", ref+"");

		Element firstElement = relation.getPositioning().getTerm().get( 0 ).getElement( annotation );
		Element secondElement = relation.getPositioning().getTerm().get( 1 ).getElement( annotation );

		firstElement.setFeature( "REF", ref + "");
		secondElement.setFeature( "REF", ref + "");

		if(firstElement instanceof Schema){
			setRefOnUnit( annotation, (Schema) firstElement, ref);
		}
		if(secondElement instanceof Schema){
			setRefOnUnit( annotation, (Schema) secondElement, ref);
		}

	}

	private static void setRefOnUnit(Annotation annotation, Schema schema, int currentRef){
		List<EmbeddedUnit> embeddedUnitList = schema.getPositioning().getEmbeddedUnit();
		for(int e = 0; e < embeddedUnitList.size(); e++){
			embeddedUnitList.get( e ).getElement( annotation ).setFeature( "REF" , currentRef + "");
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
