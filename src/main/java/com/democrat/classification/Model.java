package com.democrat.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class Model {

	/**
	 * model d'évaluation pour test sur un nouvel 
	 * ensemble
	 */
	private AbstractClassifier classifier;

	private String path;


	private Model( AbstractClassifier classifier ){
		this.classifier = classifier;

	}

	/**
	 * charge le fichier passé en argument
	 * et test si il contient bien la structure d'un
	 * fichier .model de Weka ou non
	 * @param pathFile chemin du fichier à tester
	 * @return vrai si c'est un .model de Weka
	 */
	public static boolean isModelFile(File file){

		//TODO		
		return true;

	}

	/**
	 * retourne un modèle appris sur un ensemble de donnée
	 * et grâce à un classifier avec ses paramètres/options déjà données
	 * @param arffFile ensemble de donnée sous format arff 
	 * @param classifier classifier étendu de ClassifierParameter
	 * @return
	 */
	public static Model learnModel(String arffFile, AbstractClassifier classifier ){
		File file = new File( arffFile );
		if( Model.isModelFile( file ) ){
			//chargement des attributs et des instances

			BufferedReader reader = null;
			try {
				reader = new BufferedReader( new FileReader( arffFile ) );
				Instances train = new Instances( reader );
				//selection du dernier attribut pour le choisir comme classe
				train.setClassIndex( train.numAttributes() - 1 );


				classifier.buildClassifier( train );

				return new Model( classifier );

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			return null;


		}else{
			//TODO reponse en retour de fichier non correct
			return null;
		}

	}


	/**
	 * Charge un fichier modele donné,
	 * retourne une erreur si le type de fichier n'est pas bon.
	 * @param modelFile 
	 * @return
	 */
	public static Model loadModel(String modelFile){
		try {
			AbstractClassifier cls = ( AbstractClassifier ) weka.core.SerializationHelper.read( modelFile );
			Model model = new Model( cls );
			model.setPath( modelFile );
			return model;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * le model retrouve la classe de chaque instance donnée
	 * retourne le résultat de toutes les instances
	 * @param unlabeled
	 * @return
	 */
	public Instances classifyInstance( Instances unlabeled ){
		// set class attribute
		unlabeled.setClassIndex(unlabeled.numAttributes() - 1);


		// create copy
		Instances labeled = new Instances( unlabeled );
		// label instances
		for (int u = 0; u < unlabeled.numInstances(); u++) {
			double clsLabel;
			try {
				clsLabel = classifier.classifyInstance( unlabeled.instance( u ) );
				labeled.instance( u ).setClassValue( clsLabel );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return labeled;
	}

	/**
	 * Sauvegarde le modèle avec le nom spécifié
	 * pas besoin d'ajouter le nom dans l'extension
	 * celle-ci est ajoutée
	 * @param fileName
	 */
	public void export(String fileName){		
		Toast.fileManager.mkdir( "generated/models" );
		try {
			SerializationHelper.write("generated/models/" + fileName + ".model", this.classifier);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Evaluation crossValidate( Instances instances, int nbFolds ){
		Evaluation eval = null;
		try {
			instances.setClassIndex( instances.numAttributes() - 1 );
			eval = new Evaluation( instances );
			eval.crossValidateModel(this.classifier, instances, nbFolds, new Random(1) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eval;

	}


}
