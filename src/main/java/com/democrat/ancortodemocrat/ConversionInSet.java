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
public class ConversionInSet {


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

	
	/**
	 * @param annotation where the REF feature will be set
	 */
	public static void toSetFromChain( Annotation annotation ){

		List<Unit> unitList = annotation.getUnit();
		int currentRef = 0;

		for(Unit unit : unitList){
			if( unit.isNew( annotation ) && ! unit.getId().contains( "TXT_IMPORTER") &&
					! unit.isContainedInSchema( annotation ) ){
				
				if( unit.isAssociative( annotation )){
					//test if already done or not
					//because a associative unit has NEW to YES
					if( ! unit.getFeature( "REF" ).equalsIgnoreCase( "NULL" )){
						continue;
					}
				}
				//starting set new REF on each ref and unit associated
				treatUnitFromChain( annotation, unit, currentRef, null );
				currentRef++;
			}
		}
		
		List<Schema> schemaList = annotation.getSchema();
		for(Schema schema : schemaList){
			if( schema.isNew( annotation ) && ! schema.getId().contains( "TXT_IMPORTER") &&
					! schema.isContainedInSchema( annotation ) ){
				if( schema.isAssociative( annotation )){
					//test if already done or not
					//because a associative unit has NEW to YES
					if( ! schema.getFeature( "REF" ).equalsIgnoreCase( "NULL" )){
						continue;
					}
				}

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

		//logger.debug( "UNIT : "+unit+" size "+relationAssociated.size());
		

		if( relationAssociated.size() == 1){
			//last or first unit
			//if last stop it
			if( relationAssociated.get( 0 ).equals( lastRelation )){
				//if the relation is the same that before
				//no need to go further
				setRefFeatureFromChain(annotation, relationAssociated.get( 0 ), currentRef);
				return;
			}else{
				//first
				setRefFeatureFromChain(annotation, relationAssociated.get( 0 ), currentRef);

			}
			lastRelation = relationAssociated.get( 0 );
			//currentUnit = (Unit) relationAssociated.get( 0 ).getOtherElement(annotation, currentUnit);

			//it means that the chain contains only one relation, so with two units/schemas
			//not need to go further
			if( annotation.getRelationContaining( unit ).size() == 1 && ! unit.isNew( annotation )){
				return;
			}
			treatUnitFromChain( annotation, (Unit) relationAssociated.get( 0 ).getOtherElement(annotation, unit), currentRef, relationAssociated.get( 0 ));
		}else if(relationAssociated.size() >= 2){
			
			if( lastRelation != null ){
				boolean allAreAssociative = true;
				boolean allTreated = true;
				
				for(int r = 0; r < relationAssociated.size(); r++){
					if( ! relationAssociated.get( r ).getFeature( "REF" ).equalsIgnoreCase( "NULL" ) ){
						allTreated = allTreated && true;
					}else{
						allTreated = allTreated && false;
					}
					if(relationAssociated.get( r ).getCharacterisation().getType().getValue().contains( "ASSOC") ){
						allAreAssociative = allAreAssociative && true;
					}else{
						allAreAssociative = allAreAssociative && false;
					}
				}
				
				//if the function has already done the unit 
				//equivalent to already done the relation of the unit
				if( ( allAreAssociative && allTreated ) || allTreated){
					return;
				}
				
				for(int r = 0; r < relationAssociated.size(); r++){
					setRefFeatureFromChain(annotation, relationAssociated.get( r ), currentRef);
					if( ! relationAssociated.get( r ).equals( lastRelation )){
						Element element = relationAssociated.get( r ).getOtherElement( annotation, unit);
						if( element instanceof Relation ){
							continue;
						}
						Unit otherUnit = (Unit) element;
						if( otherUnit.isNew( annotation ) ){
							continue;
						}
						treatUnitFromChain( annotation, otherUnit, currentRef, relationAssociated.get( r ) );
					}
				}
				
			}else{
				//catahore case
				
				for(int r = 0; r < relationAssociated.size(); r++){
					if( ! (relationAssociated.get( r ).getOtherElement( annotation, unit) instanceof Relation)){
						treatUnitFromChain( annotation, (Unit) relationAssociated.get( r ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( r ) );
					}
				}
				
				/**
				if( ! (relationAssociated.get( 0 ).getOtherElement( annotation, unit) instanceof Relation)){
					treatUnitFromChain( annotation, (Unit) relationAssociated.get( 0 ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( 0 ) );
				}
				if( ! (relationAssociated.get( 1 ).getOtherElement( annotation, unit) instanceof Relation)){
					treatUnitFromChain( annotation, (Unit) relationAssociated.get( 1 ).getOtherElement( annotation, unit), currentRef, relationAssociated.get( 1 ) );
				}**/
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
}
