package com.democrat.classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.democrat.ancortodemocrat.ConversionInSet;
import com.democrat.ancortodemocrat.ConversionToArff;
import com.democrat.ancortodemocrat.Corpus;
import com.democrat.ancortodemocrat.FileManager;
import com.democrat.ancortodemocrat.ParamToArff;
import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

public class Toast {

	public static FileManager fileManager;
	private static Logger logger = Logger.getLogger(Toast.class);

	public static void main(String[] args) {

		Model model = Model.loadModel("generated/models/toast.model");
		Evaluation eval = model.crossValidate( loadInstance("generated/2016_07_05_10000_no_assoc_40_60_1-4_C.arff"),
				5);
		System.out.println(eval.toSummaryString("\nResults\n========\n", true));
		System.out.println("PRECISION " + eval.precision(0)+" RAPPEL "+eval.recall(0)+" F-MESURE "+eval.fMeasure(0));
		System.out.println("PRECISION " + eval.precision(1)+" RAPPEL "+eval.recall(1)+" F-MESURE "+eval.fMeasure(1));

	}

	/**
	 * 
	 * @param corpusList liste des corpus où sont extrait les relations
	 * @param modelPath chemin du fichier model à appliquer pour faire le test
	 * @param positif nombre d'instance positive, sinon 0 et toutes les relations négatives seront prises
	 * @param negatif nombre d'instance négative, sinon 0 et toutes les relations négatives seront prises
	 * @param param Quel type de relation l'on doit prendre
	 * @param outputPath Chemin de sortie des résultats
	 * @param split Si on doit splter le/les corpus indiqué(s), sinon 0 et aucun split est appliqué
	 */
	public static void scorerTask(
			List<Corpus> corpusList,
			String modelPath,
			int positif,
			int negatif,
			ParamToArff param,
			String outputPath,
			int split){
		
		//chargement des corpus
		for(Corpus corpus : corpusList ){
			logger.info("Chargement du corpus "+corpus.getName() );
			corpus.loadAnnotation();
			corpus.loadText();
		}

		//on s'assure d'abord que le/les corpus on le champ REF ajouté
		//sinon on l'ajoute, il serviera pour extraire
		//les mentions des relations, et créer les chaînes
		//pour le scorer

		//on parcours chaque unit de chaque corpus pour voir si un unit contient ref ou non,
		//si un unit ne contient pas de ref, ref à ajouter.
		for(int c = 0; c < corpusList.size(); c++){
			List<Annotation> annotationList = corpusList.get( c ).getAnnotation();
			boolean containsREF = false;
			for(int a = 0; a < annotationList.size(); a++){
				List<Unit> unitList = annotationList.get( a ).getUnit();
				annotationList.get( a ).removeTxtImporter();
				for(int u = 0; u < unitList.size(); u++){
					if( ! unitList.get( u ).getFeature("REF").equalsIgnoreCase("NULL") ){
						//REF présent
						containsREF = true;
						break;
					}
				}
			}
			if( ! containsREF ){
				//on demande à l'utilisateur de quel type est le corpus
				//si il est en première mention ou en chaîne
				boolean answered = false;
				String paramUser = "";
				while( ! answered ){
					Scanner sc = new Scanner( System.in );
					logger.info("Le corpus " + corpusList.get( c ).getName() + " a été trouvé sans champ REF.");
					logger.info("Ce champ est nécessaire,");
					logger.info("le corpus est en première mention (p) ?");
					logger.info("Ou il est en chaîne (c) ?");

					String line = sc.nextLine();
					if(line.toLowerCase().contains("c") || line.toLowerCase().contains("p") ){
						answered = true;
						logger.info("Ajout du champ REF sur " + corpusList.get( c ).getName() + ".");
						paramUser = line.toLowerCase();
					}
				}
				if( paramUser.contains( "c" ) ){
					for(int a = 0; a < annotationList.size(); a++){
						ConversionInSet.toSetFromChain( annotationList.get( a ) );
					}
				}else{
					for(int a = 0; a < annotationList.size(); a++){
						ConversionInSet.toSetFromFirstMention( annotationList.get( a ) );
					}					
				}
			}
		}
		//REF OK
		

		//première étape séléctionner les pos/neg
		ConversionToArff conversion = new ConversionToArff( corpusList,
				positif, negatif, param, outputPath, split );

		//first step: séléction de toutes les relations du/des corpus avec
		//génération des négatives, en triant selon la ParamToArff.
		conversion.sortInstance();

		//second step: séléction de p positive, et n negative comme voulue
		conversion.selectInstance();
		
		//on écrit le fichier arff
		conversion.writeInstance();

		//on récupère les instances voulues
		//pour plus tard on sait que les instances positives sont
		//écrite en premier temps, dans l'ordre de la liste
		//et ensuite les négatives relations
		Map<Relation, Annotation> positiveRelation = conversion.getPositiveRelationSelected();
		Map<Relation, Annotation> negativeRelation = conversion.getNegativeRelationSelected();
		
		

	}

	public static Instances loadInstance(String arffFile){
		BufferedReader reader = null;
		Instances instances = null;
		try {
			reader = new BufferedReader( new FileReader( arffFile ) );
			instances = new Instances( reader );

			instances.setClassIndex( instances.numAttributes() - 1 );
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return instances;
	}

}
