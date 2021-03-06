package com.democrat.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Model class
 * @author Alexis Puret
 * @author Augustin Voisin-Marras
 */
public class Model {

	/**
	 * model d'évaluation pour test sur un nouvel 
	 * ensemble
	 */
	private AbstractClassifier classifier;

	private String path;


	/**
	 *
	 * @param classifier to use for this model
	 */
	private Model(AbstractClassifier classifier){
		this.classifier = classifier;

	}


	/**
	 * retourne un modèle appris sur un ensemble de donnée
	 * et grâce à un classifier avec ses paramètres/options déjà données
	 * @param arffFile ensemble de donnée sous format arff 
	 * @param classifier classifier étendu de ClassifierParameter
	 * @return
	 */
	public static Model learnModel(String arffFile, AbstractClassifier classifier){
		File file = new File(arffFile);
		//chargement des attributs et des instances

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(arffFile));
			Instances train = new Instances(reader);
			//selection du dernier attribut pour le choisir comme classe
			train.setClassIndex(train.numAttributes() - 1);


			classifier.buildClassifier(train);

			return new Model(classifier);

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
	}


	/**
	 * Charge un fichier modele donné,
	 * retourne une erreur si le type de fichier n'est pas bon.
	 * @param modelFile 
	 * @return
	 */
	public static Model loadModel(String modelFile){
		try {
			AbstractClassifier cls = (AbstractClassifier) weka.core.SerializationHelper.read(modelFile);
			Model model = new Model(cls);
			model.setPath(modelFile);
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
	 * @param unlabeled: Liste des instances à classifier
	 * @return Instances object containing classified instances
	 */
	public void classifyInstance(Instances unlabeled){
		// set class attribute
		unlabeled.setClassIndex(unlabeled.numAttributes() - 1);


		// label instances
		for (int u = 0; u < unlabeled.numInstances(); u++) {
			double clsLabel;
			double[] _;
			try {
				clsLabel = classifier.classifyInstance(unlabeled.instance(u));
				unlabeled.instance(u).setClassValue(clsLabel); // 0 OU 1
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * le model retrouve la probabilité que la classe soit COREF
	 *  pour chaque instance donnée
	 * @param unlabeled : Liste des instances à classifier
	 * @return Instances object containing classified instances with classification score
	 */
	public Instances classifyInstanceProba(Instances unlabeled){
		// set class attribute
		unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

		Instances inst_out = new Instances(unlabeled);
		inst_out.insertAttributeAt(new Attribute("P(CLASS)"),inst_out.numAttributes()-1);

		// label instances
		for (int u = 0; u < unlabeled.numInstances(); u++) {
			double clsLabel;
			double[] _;
			try {
				clsLabel = classifier.classifyInstance(unlabeled.instance(u));
				inst_out.instance(u).setClassValue(clsLabel); // 0 OU 1
				clsLabel = classifier.distributionForInstance(unlabeled.instance(u))[(int)clsLabel];
				inst_out.instance(u).setValue(inst_out.numAttributes()-2,clsLabel); // 0 < x < 1
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return inst_out;
	}

	public Evaluation crossValidate(Instances instances, int nbFolds){
		Evaluation eval = null;
		try {
			instances.setClassIndex(instances.numAttributes() - 1);
			eval = new Evaluation(instances);
			eval.crossValidateModel(this.classifier, instances, nbFolds, new Random(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eval;

	}


}
