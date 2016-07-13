package com.democrat.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class Model {

	/**
	 * model d'évaluation pour test sur un nouvel 
	 * ensemble
	 */
	private Evaluation evaluation;
	private ClassifierParameter classifierParam;


	public Model(Evaluation evaluation, ClassifierParameter classifier){
		this.evaluation = evaluation;
		this.classifierParam = classifier;
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
	 * et grâce à un classifier avec ses paramètres
	 * @param arffFile ensemble de donnée sous format arff 
	 * @param classifier classifier étendu de ClassifierParameter
	 * @return
	 */
	public static Model learnModel(String arffFile, ClassifierParameter classifier ){
		File file = new File( arffFile );
		if( Model.isModelFile( file ) ){
			//chargement des attributs et des instances

			BufferedReader reader = null;
			try {
				reader = new BufferedReader( new FileReader( arffFile ) );
				Instances train = new Instances( reader );
				//selection du dernier attribut pour le choisir comme classe
				train.setClassIndex( train.numAttributes() - 1 );

				Evaluation eval = new Evaluation( train );

				classifier.buildClassifier( train );

				return new Model(eval, classifier);

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
	 * Charge un fichier modele donnée,
	 * retourne une erreur si le type de fichier n'est pas bon.
	 * @param modelFile 
	 * @return
	 */
	public static Model loadModel(String modelFile){

		return null;
	}


	public void useModel( Instances instances){
		return;
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
		Instances labeled = new Instances(unlabeled);
		// label instances
		for (int i = 0; i < unlabeled.numInstances(); i++) {
			double clsLabel;
			try {
				clsLabel = classifierParam.getClassifier().classifyInstance(unlabeled.instance(i));

				labeled.instance(i).setClassValue(clsLabel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return labeled;
	}

	public void export(){

	}

}
