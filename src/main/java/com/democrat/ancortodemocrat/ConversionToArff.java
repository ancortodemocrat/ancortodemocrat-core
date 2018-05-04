package com.democrat.ancortodemocrat;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Element;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;
import com.democrat.ancortodemocrat.feature.CalculateFeature;

public class ConversionToArff implements Runnable{
	
	
	public final static String ARFF_ATTRIBUTE = "@RELATION coreference\n"+
			"@ATTRIBUTE m1_type {N, PR, NULL}\n"+
			"@ATTRIBUTE m2_type {N, PR, NULL}\n"+
			"@ATTRIBUTE m1_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, NULL, UNK}\n"+
			"@ATTRIBUTE m2_def {INDEF, EXPL, DEF_SPLE, DEF_DEM, NULL, UNK}\n"+
			"@ATTRIBUTE m1_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m2_genre {M, F, UNK, NULL}\n"+
			"@ATTRIBUTE m1_nombre {SG, PL, UNK, NULL}\n"+
			"@ATTRIBUTE m2_nombre {SG, PL, UNK, NULL}\n"+
			/**
			"@ATTRIBUTE m1_previous string\n"+
			"@ATTRIBUTE m2_previous string\n"+
			"@ATTRIBUTE m1_next string\n"+
			"@ATTRIBUTE m2_next string\n"+
			"@ATTRIBUTE m1_spk string\n"+
			"@ATTRIBUTE m2_spk string\n"+
			 **/
//			 "@ATTRIBUTE m1_new {YES, NO, UNK, NULL}\n"+
//			 "@ATTRIBUTE m2_new {YES, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m1_en {PERS, FONC, LOC, ORG, PROD, TIME, AMOUNT, EVENT, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE m2_en {PERS, FONC, LOC, ORG, PROD, TIME, AMOUNT, EVENT, NO, UNK, NULL}\n"+
			 "@ATTRIBUTE id_form {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_subform {YES, NO, NA}\n"+
			 "@ATTRIBUTE incl_rate real\n"+
			 "@ATTRIBUTE com_rate real\n"+
			 "@ATTRIBUTE id_def {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_type {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_en {YES, NO, NA}\n"+
			 "@ATTRIBUTE id_genre {YES, NO, UNK}\n" +
			 "@ATTRIBUTE id_nombre {YES, NO, UNK}\n" +
			 "@ATTRIBUTE id_spk {YES, NO, NA}\n" +
			 "@ATTRIBUTE distance_mention real\n" +
			 "@ATTRIBUTE distance_turn real\n" +
			 "@ATTRIBUTE distance_word real\n" + 
			 "@ATTRIBUTE distance_char real\n" +
		//	 "@ATTRIBUTE id_new {YES, NO, NA}\n" +
			 "@ATTRIBUTE EMBEDDED {YES, NO, NA}\n" +
			 "@ATTRIBUTE id_previous {YES, NO, NA}\n" +
			 "@ATTRIBUTE id_next {YES, NO, NA}\n" +
			 "@ATTRIBUTE class {COREF, NOT_COREF}\n" +
			 "@DATA";


	private static Logger logger = Logger.getLogger(ConversionToArff.class);

	/**
	 * Liste des corpus dans laquelle
	 * les instances doivent être séléctionnées
	 */
	private List<Corpus> corpusList = new ArrayList<Corpus>();

	/**
	 * Paramètres de l'utilisateur
	 * qui indique combien d'instances
	 * positives et négatives doivent être
	 * extraites.
	 */
	private int positif;
	private int negatif;

	public static int countPositiveRelation = 0;
	public static int countNegativeRelation = 0;


	/**
	 * Listes des instances séléctionnées 
	 * filtrage grâce aux paramètres
	 * C'est donc les listes finales des instances attendues
	 */
	public Map<Relation, Annotation> positiveRelationSelected = new HashMap<Relation, Annotation>();
	public Map<Relation, Annotation> negativeRelationSelected = new HashMap<Relation, Annotation>();


	private ParamToArff param;

	private String outputPath;

	private List<String> fileOuput = new ArrayList<String>();
	private int split;

	public ConversionToArff(Corpus corpus){
		this.corpusList.add( corpus );
	}

	/**
	 * 
	 * @param positif nombre d'instance de relation positive voulue
	 * @param negatif nombre d'instance de négative voulue
	 * @param param Paramètre indiquant si on garde ou non les associatives
	 * @param outputPath chemin de sortie du fichier arff
	 */
	private ConversionToArff(int positif, int negatif, ParamToArff param, String outputPath, int split){
		this.positif = positif;
		this.negatif = negatif;
		this.param = param;
		this.outputPath = outputPath;
		this.split = split;

	}

	public ConversionToArff(Corpus corpus, int positif, int negatif, ParamToArff param, String outputPath, int split){
		this(positif, negatif, param, outputPath, split );
		this.corpusList.add( corpus );
	}

	public ConversionToArff(List<Corpus> corpusList, int positif, int negatif, ParamToArff param,  String outputPath, int split){
		this(positif, negatif, param, outputPath, split );
		this.corpusList = corpusList;
	}


	public List<String> getFileOuput() {
		return fileOuput;
	}

	/**
	 * Pour une relation donnée, écrit dans un string la liste
	 * des valeurs de ses traits, et renvoie cette ligne.
	 * @param annotation Annotation nécessaire qui contient la relation pour récupérer les traits
	 * @param relation Relation concernée où les traits seront extraits
	 * @return
	 */
	private String makeRelation( Annotation annotation, Relation relation ){
		String line = "";
		Element element = relation.getElement( annotation );
		Element preElement = relation.getPreElement( annotation );
		if( element instanceof Unit && preElement instanceof Unit ){
			//m1_type
			if( preElement.getCharacterisation().getType().getValue().equals("default") ){
				line += "N";
			}else{
				line += preElement.getCharacterisation().getType().getValue();
			}
			line += " ";
			//m2_type
			if( element.getCharacterisation().getType().getValue().equals("default") ){
				line += "N";
			}else{
				line += element.getCharacterisation().getType().getValue();
			}
			line += " ";

			//m1_def
			line += preElement.getFeature( "DEF" );
			line += " ";
			//m2_def
			line += element.getFeature( "DEF" );

			line += " ";

			//m1_genre
			line += preElement.getFeature( "GENRE" );
			line += " ";
			//m2_genre
			line += element.getFeature( "GENRE" );
			line += " ";

			//m1_nombre
			line += preElement.getFeature( "NB" );
			line += " ";
			//m2_nombre
			line += element.getFeature( "NB" );
			line += " ";

//			//m1_new
//			line += preElement.getFeature( "NEW" );
//			line += " ";
//			//m2_new
//			line += element.getFeature( "NEW" );
//			line += " ";


			//m1_en
			line += preElement.getFeature( "EN" );
			line += " ";
			//m2_en
			line += element.getFeature( "EN" );
			line += " ";

			//id_form
			line += relation.getFeature( "ID_FORM" );
			line += " ";

			//id_subform
			line += relation.getFeature( "ID_SUBFORM" );
			line += " ";

			//incl_rate
			line += relation.getFeature( "INCL_RATE" );
			line += " ";

			//com_rate
			line += relation.getFeature( "COM_RATE" );
			line += " ";

			//ID_DEF
			line += relation.getFeature( "ID_DEF" );
			line += " ";

			//ID_TYPE
			line += relation.getFeature( "ID_TYPE" );
			line += " ";

			//id_en
			line += relation.getFeature( "ID_EN" );
			line += " ";

			//ID_GENRE
			line += relation.getFeature( "GENRE" );
			line += " ";

			//ID_NOMBRE
			line += relation.getFeature( "NOMBRE" );
			line += " ";

			//ID_SPK
			line += relation.getFeature( "ID_SPK" );
			line += " ";

			//distance_mention
			line += relation.getFeature( "DISTANCE_MENTION" );
			line += " ";

			//distance_turn
			line += relation.getFeature( "DISTANCE_TURN" );
			line += " ";

			//distance word
			line += relation.getFeature( "DISTANCE_WORD" );
			line += " ";

			//Distance_char 
			line += relation.getFeature( "DISTANCE_CHAR" );
			line += " ";

//			//id_new
//			line += relation.getFeature( "ID_NEW" );
//			line += " ";

			//embedded
			line += relation.getFeature( "EMBEDDED" );
			line += " ";

			//id_previous
			line += relation.getFeature( "ID_PREVIOUS" );
			line += " ";

			//id_next
			line += relation.getFeature( "ID_NEXT" );
			line += " ";

		}
		return line;
	}

	private void work( ){

		//first step: séléction de toutes les relations du/des corpus avec
		//génération des négatives, en triant selon la ParamToArff.
		sortInstance();
		//second step: séléction de p positive, et n negative comme voulue
		this.selectInstance();
		//puis écriture des instances
		this.writeInstance();

	}


	public Map<Relation, Annotation> getPositiveRelationSelected() {
		return positiveRelationSelected;
	}

	public Map<Relation, Annotation> getNegativeRelationSelected() {
		return negativeRelationSelected;
	}

	public void sortInstance(){
		for(Corpus corpus : corpusList){
			logger.info("corpus ==> " + corpus.getName() );
			for(Annotation annotation : corpus.getAnnotation()){
				annotation.removeTxtImporter();

				for( Relation relation : annotation.getRelation() ){
					if(this.param.equals( ParamToArff.NO_ASSOC )){
						//test si la relation est une associative ou non
						//si c'est le cas, next
						if(relation.getCharacterisation().getType().getValue().toLowerCase().contains( "assoc" ) ){
							continue;
						}
					}
					if(!this.param.equals(ParamToArff.ALL) && !this.param.equals(ParamToArff.NO_ASSOC)
							&& ! this.param.equals( relation.getCharacterisation().getType().getValue().toLowerCase()))
						continue;
					if( relation.getPreElement( annotation ) == null ||
							relation.getElement( annotation ) == null ){
						continue;
					}
					if( relation != null && relation instanceof Relation ){
						this.positiveRelationSelected.put(relation, annotation);
						generateNegativeRelation( corpus, annotation, relation );
					}
					//if( negativeRelation != null){
					//this.negativeRelationSelected.put(negativeRelation, annotation );
					//}
					//for positive class
					//String line = makeRelation( annotation, relation );
					/**if( ! line.isEmpty() ){
						line += "COREF";
						//writer.println( line );
						//this.positiveRelation.add( line );

						countPositiveRelation++;
						//for negative class
					}**/
				}
			}
		}
	}

	/**
	 * Séléctionne les instances positives et négatives selon les paramètres
	 * dans la liste de ceux trouvées dans les corpus
	 */
	public void selectInstance(){
		if( this.positif > this.positiveRelationSelected.size() || this.negatif > this.negativeRelationSelected.size() ){
			float diviseur = 1.f;
			if( this.positif > this.positiveRelationSelected.size() ){
				logger.info("Trop d'instances positives attendues ("+positif+") par rapport au corpus, restriction au maxium " + this.positiveRelationSelected.size() + ".");
				diviseur = (float)this.positiveRelationSelected.size() / (float)this.positif;
			}
			if( this.negatif > this.negativeRelationSelected.size() ){
				logger.info("Trop d'instances négatives (" + this.negatif + ") attendues par rapport au corpus, restriction au maxium " + this.negativeRelationSelected.size() + ".");
				diviseur = Math.min(diviseur, (float)this.negativeRelationSelected.size() / (float)this.negatif);
			}

			logger.info("Respect des pourcentages, positifs/négatifs: division par "+1.f/ diviseur);
			/*float sum = this.positif + this.negatif;
			float percentPositive = this.positif / sum;
			float percentNegative = this.negatif / sum;

			this.positif = (int) ( percentPositive * this.positiveRelationSelected.size() );
			this.negatif = (int) ( percentNegative * this.negativeRelationSelected.size() );
			percentPositive *= 100;
			percentNegative *= 100;*/
			this.positif = (int) (((float)positif) * diviseur);
			this.negatif = (int) (((float) negatif)* diviseur);

			logger.info("soit " + ( this.positif ) + " instances positives");
			logger.info("soit " + this.negatif + " instances négatives");
		}

		if(this.positif == 0){
			//on prend tout 
			//this.positiveRelationSelected = this.positiveRelation;
			//this.negativeRelationSelected = this.negativeRelation;
		}else{
			//séléction aléatoire de p positive, et n négative
			int random = 0;
			ArrayList<Integer> nbGenerated = new ArrayList<Integer>();
			//on fait une liste temporaire pour ne garder que le nombre stricte
			//de positive et négative relation
			Map<Relation, Annotation> tmpPositiveRelation = new HashMap<Relation, Annotation>();
			//Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray(new Relation[ this.positiveRelationSelected.size() ]);
			List<Relation> relationList = new ArrayList<Relation>( this.positiveRelationSelected.keySet() );
			for(int p = 0; p < this.positif; p++){
				random = AncorToDemocrat.randomNumber( 0, this.positiveRelationSelected.size() - 1);
				if( nbGenerated.contains( random ) ){
					while( nbGenerated.contains( random ) ){
						random = AncorToDemocrat.randomNumber( 0, positiveRelationSelected.size() - 1);
					}
				}
				nbGenerated.add( random );
				tmpPositiveRelation.put(relationList.get( random ), this.positiveRelationSelected.get( relationList.get( random ) ));
				//this.positiveRelationSelected.add( positiveRelationSelected.get( random ) );

			}
			//et on assigne cette liste temporaire à la liste total
			this.positiveRelationSelected = new HashMap<Relation, Annotation>( tmpPositiveRelation );
			tmpPositiveRelation.clear();
			nbGenerated.clear();
			//de même pour les négatifs
			Map<Relation, Annotation> tmpNegativeRelation = new HashMap<Relation, Annotation>();
			//relationArray = (Relation[]) this.negativeRelationSelected.keySet().toArray( new Relation[ this.negativeRelationSelected.size() ]);
			relationList = new ArrayList<Relation>( this.negativeRelationSelected.keySet() );
			for(int p = 0; p < this.negatif; p++){
				random = AncorToDemocrat.randomNumber( 0, this.negativeRelationSelected.size() - 1);
				if( nbGenerated.contains( random ) ){
					while( nbGenerated.contains( random ) ){
						random = AncorToDemocrat.randomNumber( 0, negativeRelationSelected.size() - 1);
					}
				}
				nbGenerated.add( random );

				tmpNegativeRelation.put(relationList.get( random ), this.negativeRelationSelected.get( relationList.get( random ) ));
				//this.positiveRelationSelected.add( positiveRelationSelected.get( random ) );

			}
			this.negativeRelationSelected = tmpNegativeRelation;
		}
	}

	public void writeInstance(){
		PrintWriter writer = null;
		PrintWriter writer_links = null;

		String fileName;
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
				DateFormat.SHORT,
				DateFormat.SHORT);
		fileName = shortDateFormat.format( new Date() );
		logger.info( fileName );
		fileName = fileName.replace(" ", "_");
		fileName = fileName.replace("/", "_");
		fileName = fileName.replace(":", "H");

		// Si le paramètre NOTCOREF est activé, on ne ressort que les relations not_coref
		
		if(this.param.equals( ParamToArff.NOTCOREF )){
						
			//this.positif = this.positiveRelationSelected.size();
			this.negatif = this.negativeRelationSelected.size();
			//ajout nombre de pos/neg à la fin du nom
			this.outputPath += "_" + this.positif + "_" + this.negatif;
			try {
				writer = new PrintWriter(this.outputPath + fileName + ".arff", "UTF-8");
				writer_links = new PrintWriter(this.outputPath + fileName + ".idff", "UTF-8"); // instance id file format
				this.fileOuput.add( this.outputPath + fileName + ".arff" );
				writer.println( ARFF_ATTRIBUTE );
				writer.println("");

			//	Set<Relation> set = positiveRelationSelected.keySet();
//				for( Relation r : set ){
//					String line = this.makeRelation(this.positiveRelationSelected.get( r ), r );
//					writer.println( line + "COREF" );
//				}

				Set <Relation> set_negatif = negativeRelationSelected.keySet();
				for( Relation r : set_negatif ){
					String line = this.makeRelation(this.negativeRelationSelected.get( r ), r );
					writer.println( line + "NOT_COREF" );
					writer_links.println(r.getId());
				}
				/**Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray();
				for(int p = 0; p < this.positiveRelationSelected.size(); p++){
					String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
					writer.println( line + "COREF" );			
				}**/
				/**relationArray = (Relation[]) this.negativeRelationSelected.keySet().toArray();
				for(int n = 0; n < this.negativeRelationSelected.size(); n++){
					String line = this.makeRelation(this.negativeRelationSelected.get( relationArray[ n ] ), relationArray[ n ] );
					writer.println( line + "NOT_COREF" );
				}**/

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(writer != null){
					writer.close();
				}
				if(writer_links != null){
					writer_links.close();
				}
			}
		}
			
		
		else{
		if(this.positif == 0){
			//tout prendre
			if( split > 0 ){
				//sauf si on doit splitter le/les corpus

				for(int f = 1; f < split + 1; f++){
					try {
						writer = new PrintWriter(this.outputPath + fileName + "_" + f + ".arff", "UTF-8");
						writer_links = new PrintWriter(this.outputPath + fileName + "_" + f + ".idff", "UTF-8");
						this.fileOuput.add( this.outputPath + fileName + "_" + f + ".arff" );
						writer.println( ARFF_ATTRIBUTE );
						writer.println("");

						//écriture instances positives
						int start = (f - 1) * this.positiveRelationSelected.size() / split;
						int end = start + this.positiveRelationSelected.size() / split;

						Relation[] relationArray =  (Relation[]) positiveRelationSelected.keySet().toArray( new Relation[ positiveRelationSelected.size() ] );
						
						for( int l = start; l < end; l++){
							int idElement = relationArray[ l ].getElement( this.positiveRelationSelected.get( relationArray[ l ] ) ).getIdMention();
							int idPreElement = relationArray[ l ].getPreElement( this.positiveRelationSelected.get( relationArray[ l ] ) ).getIdMention();
							String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ l ] ), relationArray[ l ] );
							writer.println( line + "COREF");
							writer_links.println(relationArray[l].getId());
							if(relationArray[l].getId() == null)
								new Exception("null id").printStackTrace();
						}

						//écriture instances négatives
						relationArray = (Relation[]) negativeRelationSelected.keySet().toArray( new Relation[ negativeRelationSelected.size() ] );
						start = (f - 1) * this.negativeRelationSelected.size() / split;
						end = start + this.negativeRelationSelected.size() / split;
						for( int l = start; l < end; l++){
							int idElement = relationArray[ l ].getElement( this.negativeRelationSelected.get( relationArray[ l ] ) ).getIdMention();
							int idPreElement = relationArray[ l ].getPreElement( this.negativeRelationSelected.get( relationArray[ l ] ) ).getIdMention();
							String line = this.makeRelation(this.negativeRelationSelected.get( relationArray[ l ] ), relationArray[ l ] );
							writer.println( line + "NOT_COREF" );
							writer_links.println(relationArray[l].getId());
							if(relationArray[l].getId() == null)
								new Exception("null id").printStackTrace();
						}

					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						if(writer != null){
							writer.close();
						}
						if(writer_links != null){
							writer_links.close();
						}
					}
				}
			}else{
				this.positif = this.positiveRelationSelected.size();
				this.negatif = this.negativeRelationSelected.size();
				//ajout nombre de pos/neg à la fin du nom
				this.outputPath += "_" + this.positif + "_" + this.negatif;
				try {
					writer = new PrintWriter(this.outputPath + fileName + ".arff", "UTF-8");
					writer_links = new PrintWriter(this.outputPath + fileName + ".idff", "UTF-8");
					this.fileOuput.add( this.outputPath + fileName + ".arff" );
					writer.println( ARFF_ATTRIBUTE );
					writer.println("");

					Set<Relation> set = positiveRelationSelected.keySet();
					for( Relation r : set ){
						String line = this.makeRelation(this.positiveRelationSelected.get( r ), r );
						writer.println( line + "COREF" );
						writer_links.println(r.getId());
						if(r.getId() == null)
							new Exception("null id").printStackTrace();
					}

					set = negativeRelationSelected.keySet();
					for( Relation r : set ){
						String line = this.makeRelation(this.negativeRelationSelected.get( r ), r );
						writer.println( line + "NOT_COREF" );
						writer_links.println(r.getId());
						if(r.getId() == null)
							new Exception("null id").printStackTrace();
					}
					/**Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray();
					for(int p = 0; p < this.positiveRelationSelected.size(); p++){
						String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
						writer.println( line + "COREF" );			
					}**/
					/**relationArray = (Relation[]) this.negativeRelationSelected.keySet().toArray();
					for(int n = 0; n < this.negativeRelationSelected.size(); n++){
						String line = this.makeRelation(this.negativeRelationSelected.get( relationArray[ n ] ), relationArray[ n ] );
						writer.println( line + "NOT_COREF" );
					}**/

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					if(writer != null){
						writer.close();
					}
					if(writer_links != null){
						writer_links.close();
					}
				}
			}
		}else{
			//les instances sont déjà séléctionnées, juste besoin de les écrire
			try {
				this.outputPath += fileName;


				writer = new PrintWriter(this.outputPath + ".arff", "UTF-8");
				writer_links = new PrintWriter(this.outputPath + ".idff", "UTF-8");
				this.fileOuput.add( this.outputPath + ".arff" );
				writer.println( ARFF_ATTRIBUTE );
				writer.println("");

				Relation[] relationArray = (Relation[]) this.positiveRelationSelected.keySet().toArray( new Relation[ this.positiveRelationSelected.size() ] );
				for(int p = 0; p < this.positiveRelationSelected.size(); p++){
					int idElement = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
					int idPreElement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getIdMention();
					String line = this.makeRelation(this.positiveRelationSelected.get( relationArray[ p ] ), relationArray[ p ] );
					writer.println( line + "COREF" );	
					writer_links.println(relationArray[p].getId());
					if(relationArray[p].getId() == null)
						new Exception("null id").printStackTrace();

					
//					String feature_id_element = relationArray[ p ].getElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getFeature("GENRE");
//					logger.info("Le feature de element est: " + feature_id_element);
//					
//					String feature_id_preelement = relationArray[ p ].getPreElement( this.positiveRelationSelected.get( relationArray[ p ] ) ).getFeature("GENRE");
//					logger.info("Le feature de pre_element est: " + feature_id_preelement);
//
//					String feature_element = relationArray[p].getFeature("GENRE");
//					logger.info("Le feature de la relation est: " + feature_element);
//					logger.info("___________________");

					
					
				}
				relationArray = (Relation[]) this.negativeRelationSelected.keySet().toArray( new Relation[ this.negativeRelationSelected.size() ] );
				for(int l = 0; l < this.negativeRelationSelected.size(); l++){
					int idElement = relationArray[ l ].getElement( this.negativeRelationSelected.get( relationArray[ l ] ) ).getIdMention();
					int idPreElement = relationArray[ l ].getPreElement( this.negativeRelationSelected.get( relationArray[ l ] ) ).getIdMention();
					String line = this.makeRelation(this.negativeRelationSelected.get( relationArray[ l ] ), relationArray[ l ] );
					writer.println( line + "NOT_COREF" );
					String rid = relationArray[l].getId();
					writer_links.println(relationArray[l].getId());
					if(relationArray[l].getId() == null)
						new Exception("null id").printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(writer != null){
					writer.close();
				}
				if(writer_links != null){
					writer_links.close();
				}
			}
		}
		
		}
		
	}	


	/**
	 * Génère entre une à trois relations négatives, en prenant comme unité, le premier élément 
	 * de la relation.
	 * @param corpus Corpus sur lequel la relation est basée
	 * @param annotation Annotation qui contient la relation
	 * @param relation relation à partir de laquelle sera generée entre 1 à 3 relation négative
	 */
	private void generateNegativeRelation(Corpus corpus, Annotation annotation, Relation relation) {
		int negativeRelationToGenerate = AncorToDemocrat.randomNumber(1, 3);
		List<Unit> unitList = annotation.getUnit();
		for(int turn = 0; turn < negativeRelationToGenerate; turn++){
			boolean done = false;
			int attempt = 0;
			while( ! done ){
				int unitIdRandom = AncorToDemocrat.randomNumber(0, unitList.size() - 1 );
				//if( ! unitList.contains( unitIdRandom ) ){
				//negative relation generated
				if( ! unitList.get( unitIdRandom ).getFeature( "REF" ).equals( relation.getFeature( "REF" ) ) ){
					//thhttp://www.univ-orleans.tk/unit is not in the chain of relation
					// we create a new relation between the unit and preElement of the relation
					Unit unit = (Unit) relation.getElement( annotation );
					if( unit != null ){
						Relation newRelation = new Relation();
						newRelation.setId("generated_rel_("+unit.getId()+','+unitList.get( unitIdRandom ).getId()+')');
						newRelation.addUnit( unit );
						newRelation.addUnit( unitList.get( unitIdRandom ) );

						CalculateFeature calculateFeature = new CalculateFeature( corpus, "" );
						calculateFeature.calculateFeatureOnRelation(annotation, newRelation);

						//then calculate feature of the new relation
						/**String line = makeRelation( annotation, newRelation );
					if( ! line.isEmpty() ){
						line += "NOT_COREF";
						//writer.println( line );
						this.negativeRelation.add( line );
						countNegativeRelation++;
					}**/
						//return newRelation;
						this.negativeRelationSelected.put(newRelation, annotation );
					}
				}
				//}
				if( attempt > 15 ){
					//safe case
					break;
				}
				attempt++;
			}
		}
		//return null;
	}

@Override
	public void run() {
		//charger chaque corpus..

		for(Corpus corpus : this.corpusList ){
			logger.info("Chargement du corpus " + corpus.getName() );
			corpus.loadAnnotation();
			corpus.loadText();
		}

		this.work( );
		logger.info("[" + this.outputPath + "] arff file writed.");
		logger.info("COREF : " + this.positiveRelationSelected.size() );
		logger.info("NOT-COREF: " + this.negativeRelationSelected.size() );
	}

}
