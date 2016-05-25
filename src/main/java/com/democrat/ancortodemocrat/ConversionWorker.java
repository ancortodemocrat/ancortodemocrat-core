package com.democrat.ancortodemocrat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Schema;
import com.democrat.ancortodemocrat.element.Type;
import com.democrat.ancortodemocrat.element.Unit;
import com.democrat.ancortodemocrat.positioning.PositioningRelation;
import com.democrat.ancortodemocrat.treetagger.TokenConvertRelationHandler;
import com.democrat.ancortodemocrat.treetagger.TokenConvertRelationHandlerException;
import com.democrat.ancortodemocrat.treetagger.TreeTaggerManager;

public class ConversionWorker implements Runnable{

	private static Logger logger = Logger.getLogger(ConversionWorker.class);

	private Corpus corpus;

	private int countIndirect;

	private int countIndirectWithDeal;

	public ConversionWorker( Corpus corpus ){
		this.corpus = corpus;
		//treeTaggerManager = new TreeTaggerManager();
	}

	private void work(){
		logger.info("Start converting: [" + corpus.getName() +"]");


		countIndirect = 0;
		countIndirectWithDeal = 0;

		for( int a = 0; a < this.corpus.getAnnotation().size(); a++ ){

			logger.info("[" + corpus.getName() +"] Converting file: "+(a + 1)+"/"+this.corpus.getAnnotation().size() + " : " + this.corpus.getAnnotation().get( a ).getFileName() );
			Annotation annotation = this.corpus.getAnnotation().get( a );
			this.convertRelationToChain( annotation );
			ConversionInSet.toSetFromChain( annotation );
		}


		logger.info("[" + corpus.getName() +"] Nombre d'indirect: " + countIndirect);
		logger.info("[" + corpus.getName() +"] Nombre d'indirect avec accord en nombre: " + countIndirectWithDeal);

		logger.info("[" + corpus.getName() +"] done !");
		corpus.setDone( true );
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

			if(relation.getCharacterisation().getType().getValue().contains( "ASSOC" )){
				//associative doesnt change
				newRelations.add( newRelation );
				continue;				
			}


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


					//count every (NO, IND, IND)
					if(relation.getCharacterisation().getType().getValue().equalsIgnoreCase( "INDIRECTE" ) && 
							preRelation.getCharacterisation().getType().getValue().equalsIgnoreCase( "INDIRECTE" )){
						countIndirect++;
						if(relation.getFeature( "NOMBRE" ).equalsIgnoreCase( "YES" ) &&
								preRelation.getFeature( "NOMBRE" ).equalsIgnoreCase( "YES" )){

							countIndirectWithDeal++;
						}
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
						//logger.debug(newRelation.getId() );
						convertCharacterisationType( annotation, newRelation, preRelation );
						convertCharacterisationIDLOC( annotation, newRelation, preRelation );
						convertCharacterisationFeature( annotation, newRelation );
					}else{
						//TODO relation to relation
						logger.debug("Relation to relation in "+this.corpus.getName() + " annotation " + annotation.getFileName());
					}


				}
			}

			newRelations.add( newRelation );
		}

		annotation.setRelation( newRelations );
	}

	/**
	 * recalculates the characterisation(type) 
	 * in comparaison with the preRelation not converted
	 * @param annotaiton
	 * @param relation new relation to convert
	 * @param preRelation old relation before 
	 */
	private void convertCharacterisationType( Annotation annotation, Relation relation, Relation preRelation ){
		String currentType = relation.getCharacterisation().getType().getValue();
		if(preRelation == null){
			return;
		}
		String preType = preRelation.getCharacterisation().getType().getValue();

		//PR == ANAPHORE
		if(currentType.equalsIgnoreCase( "DIRECTE" ) && preType.equalsIgnoreCase( "INDIRECTE" )){
			//(NO, DIR, IND) --> DIR
			relation.getCharacterisation().setType( new Type("INDIRECTE") );
		}else if(currentType.equalsIgnoreCase("DIRECTE") && preType.equalsIgnoreCase( "ANAPHORE" )){
			//(NO, DIR, PR) --> IND
			relation.getCharacterisation().setType( new Type( "INDIRECTE" ));
		}else if(currentType.equalsIgnoreCase("INDIRECTE") && preType.equalsIgnoreCase( "INDIRECTE" )){
			//TODO (NO, INDIR, INDIR) --> ?
			Text text = corpus.getText( annotation.getFileName() );
			if(text != null){
				Element element = relation.getElement( annotation );
				Element preElement = relation.getPreElement( annotation );
				if(element instanceof Unit && preElement instanceof Unit){
					Unit unit = (Unit) element;
					Unit preUnit = (Unit) preElement;
					
					
					//be careful, on unit we have number named NB 
					//and on relation number named NOMBRE
					String currentNb = unit.getFeature( "NB" );
					String preNb = preUnit.getFeature( "NB" );

					String currentGenre = unit.getFeature( "GENRE" );
					String preGenre = preUnit.getFeature( "GENRE" );
					
					String firstMention = text.getContentFromUnit(annotation, unit);
					String secondMention = text.getContentFromUnit(annotation, preUnit);

					//if one unit is a schema check every unit
					//for this schema to find the genre or nb
					//one of all units of schema should'nt be null
					//on these features
					if(unit instanceof Schema){
						Schema schema = (Schema) unit;
						currentNb = schema.getFeature(annotation, "NB" );
						currentGenre = schema.getFeature(annotation, "GENRE");
					}
					if(preUnit instanceof Schema){
						Schema schema = (Schema) preUnit;
						preNb = schema.getFeature(annotation, "NB" );
						preGenre = schema.getFeature(annotation, "GENRE");
					}

					if( ! currentNb.equalsIgnoreCase( preNb ) || ! currentGenre.equalsIgnoreCase( preGenre ) ){
						//no agreement number or genre
						// (NO, INDIR, INDIR) --> INDIR
						relation.getCharacterisation().setType( new Type("INDIRECTE") );
						return;
					}else if( firstMention.equalsIgnoreCase( secondMention )){
						//same word(s)
						// (NO, INDIR, INDIR) --> DIR
						relation.getCharacterisation().setType( new Type("DIRECTE") );				
					}else{
						// (NO, INDIR, INDIR) --> TreeTrager
						if( unit instanceof Schema || preUnit instanceof Schema){
							//TODO (no, INDIR, INDIR) with schema
							return;
						}
						
						
						//logger.debug("[" + relation.getId() + "] need compare: "+firstMention+" - " + secondMention);
						logger.info("[" + corpus.getName() +"] call TreeTager to check (INDIRECT, INDIRECT) "+ firstMention +"::"+ secondMention +"on relation: " + relation.getId());
						
						TokenConvertRelationHandler handler = new TokenConvertRelationHandler( relation, firstMention, secondMention );
						while( ! handler.isDone( ) ){
							try {
								Thread.sleep( 2 );
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						//logger.debug("back from "+firstMention + "::" + secondMention + " --> " + relation.getCharacterisation().getType().getValue() );
					}

				}

				//logger.debug("ID unit: "+unit.getId());
				//logger.debug("sub "+text.getContent().substring(((int)unit.getStart( annotation )), ((int)unit.getEnd( annotation ))));
				//this.countIndirect++;
			}
		}

	}

	private void convertCharacterisationIDLOC( Annotation annotation, Relation relation, Relation preRelation ){
		String currentIDLOC = relation.getFeature( "ID_LOC" );
		if(preRelation == null){
			return;
		}
		String preIDLOC = preRelation.getFeature( "ID_LOC" );

		if(currentIDLOC.equalsIgnoreCase( "YES" ) && preIDLOC.equalsIgnoreCase( "NO" )){
			relation.setFeature( "ID_LOC", "NO" );
		}else if(currentIDLOC.equalsIgnoreCase( "NO" ) && preIDLOC.equalsIgnoreCase( "NO" )){
			relation.setFeature( "ID_LOC", "YES" );
		}else if(preIDLOC.equalsIgnoreCase( "UNK" )){
			relation.setFeature( "ID_LOC", "UNK" );
		}
	}


	/** Recalculate features of the relation
	 * <ul>
	 * <li>NB</li>
	 * <li>GENRE</li>
	 * </ul>
	 **/
	private void convertCharacterisationFeature(Annotation annotation, Relation relation){
		//type
		//nb
		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if(element instanceof Unit && preElement instanceof Unit){
			Unit unit = (Unit) element;
			Unit preUnit = (Unit) preElement;
			//be careful, on unit we have number named NB 
			//and on relation number named NOMBRE
			String currentNb = unit.getFeature( "NB" );
			String preNb = preUnit.getFeature( "NB" );

			String currentGenre = unit.getFeature( "GENRE" );
			String preGenre = preUnit.getFeature( "GENRE" );

			//if one unit is a schema check every unit
			//for this schema to find the genre or nb
			//one of all units of schema should'nt be null
			//on these features
			if(unit instanceof Schema){
				Schema schema = (Schema) unit;
				currentNb = schema.getFeature(annotation, "NB" );
				currentGenre = schema.getFeature(annotation, "GENRE");
				/**List<Unit> list = schema.getUnitList( annotation );
				for(int u = 0; u < list.size(); u++){
					if( ! list.get( u ).getFeature( "NB" ).equalsIgnoreCase( "NULL" ) ){
						currentNb = list.get( u ).getFeature( "NB" );
						currentGenre = list.get( u ).getFeature( "GENRE" );
					}
				}**/
			}
			if(preUnit instanceof Schema){
				Schema schema = (Schema) preUnit;
				preNb = schema.getFeature(annotation, "NB" );
				preGenre = schema.getFeature(annotation, "GENRE");

				/**
				List<Unit> list = schema.getUnitList( annotation );
				for(int u = 0; u < list.size(); u++){
					if( ! list.get( u ).getFeature( "NB" ).equalsIgnoreCase( "NULL" ) ){
						preNb = list.get( u ).getFeature( "NB" );
						preGenre = list.get( u ).getFeature( "GENRE" );
					}
				}**/

			}

			if( currentNb != null && preNb != null){
				if( currentNb.equalsIgnoreCase( preNb )){
					//same nb, set YES
					relation.setFeature( "NOMBRE", "YES");
				}else{
					//oposite, set NO
					relation.setFeature( "NOMBRE", "NO");
				}
			}else{
				//UNK
				relation.setFeature( "NOMBRE", "UNK");
			}

			if(currentGenre != null && preGenre != null){
				if( currentGenre.equalsIgnoreCase( preGenre )){
					//same nb, set YES
					relation.setFeature( "GENRE", "YES" );
				}else{
					//oposite, set NO
					relation.setFeature( "GENRE", "NO" );
				}
			}else{
				relation.setFeature( "GENRE", "UNK" );
			}

		}
	}

	@Override
	public void run() {
		this.work();
		this.corpus.export();
	}

	public void start(){
		Thread th = new Thread( this );
		th.start();
	}

	public Corpus getCorpus() {
		return this.corpus;
	}

}