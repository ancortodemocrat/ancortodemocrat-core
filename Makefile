##	PARAMETERS
ROOT=/tp/Augustin# Dossier de travail (>1 Go libre)

ALGO=J48

#CORPUS_ZIP=./Ancor-Centre-CC-BY-NC-SA.zip
CORPUS_ZIP=./Donnees_maj.zip# Tableau6, Tableau7,
#CORPUS_ZIP=./Donnees_corpus.zip# Tableau5,

CORPUS_NAME=T7

SCORERS=muc bcub# ceafe blanc

NUM_TEST=1

# ANCOR_SMALL_SELECT_AC=$(CORPUS_SRC)/corpus_ESLO/ac_fichiers/00[4]*
# ANCOR_SMALL_SELECT_AA=$(CORPUS_SRC)/corpus_ESLO/aa_fichiers/00[4]*
# ANCOR_SMALL_SELECT_AC=$(CORPUS_SRC)/Données_corpus/Tableau6/corpus_ESLO_apprentissage/ac_fichiers/*#Tableau6
# ANCOR_SMALL_SELECT_AA=$(CORPUS_SRC)/Données_corpus/Tableau6/corpus_ESLO_apprentissage/aa_fichiers/*#Tableau6
# ANCOR_SMALL_SELECT_AC=$(CORPUS_SRC)/Données_corpus/Tableau5/corpus_apprentissage/ac_fichiers/*#Tableau5
# ANCOR_SMALL_SELECT_AA=$(CORPUS_SRC)/Données_corpus/Tableau5/corpus_apprentissage/aa_fichiers/*#Tableau5


## /PARAMETERS

CORPUS_SRC=$(ROOT)/Ancor

WEKA_CLASSIFIER=weka.classifiers
J48=trees.J48
SMO=functions.SMO
NAIVES_BAYES=bayes.NaiveBayes
ALGO_CLASS=$(WEKA_CLASSIFIER).$($(ALGO))
TRAINING_DISTRIB=1500 1075
TEST_PARAMS=-q 1000 2700
SCORE_DISTRIB=2757 3861

JAR_F=./target/ancor2-0.0.1-SNAPSHOT-jar-with-dependencies.jar
WEKA_JAR=~/bin/weka*/weka.jar
ANCOR2=java -jar $(JAR_F)

ANCOR_SMALL=$(CORPUS_SRC)/$(CORPUS_NAME)

GENERATED=$(ROOT)/generated/$(CORPUS_NAME)
FEATURE=$(GENERATED)/feature
ARFF=$(GENERATED)/arff
MODEL=$(GENERATED)/model
CALLSCORER=$(GENERATED)/callscorer

SMALL_SRC=$(ANCOR_SMALL)
CORPUS=$(GENERATED)/chain/
TRAIN_ARFF=train
TEST_ARFF=test

include *.mk


install:
	git submodule init
	git submodule update
	mvn install

expe:	init-env gen-corp expe-feature expe-model expe-scorer

gen-all: init-env gen-corp features arff gen-model

info:
	@echo CORPUS=$(CORPUS)
	@echo CORPUS_NAME=$(CORPUS_NAME)
	@echo GENERATED=$(GENERATED)
	@echo FEATURE=$(FEATURE)
	@echo ARFF=$(ARFF)
	@echo MODEL=$(MODEL)
	@echo CALLSCORER=$(CALLSCORER)
	@echo ALGO=$(ALGO)
	@echo ALGO_CLASS=$(WEKA_CLASSIFIER).$($(ALGO))

clean-all:
	$(RM) -r $(GENERATED)

clean-chain: clean-features
	$(RM) -r $(GENERATED)/chain/*

clean-cs:
	$(RM) -r $(CALLSCORER)/*

clean-arff: clean-model
	$(RM) -r $(ARFF)/*

clean-features: clean-arff
	$(RM) -r $(FEATURE)/*

clean-model: clean-cs
	$(RM) -r $(MODEL)/*

chain:
	$(ANCOR2) chain $(SMALL_SRC) -i $(CORPUS) > log/debug.txt

gen-corp:
	cp -r $(SMALL_SRC)/* $(CORPUS)

features:
	$(ANCOR2)  feature p $(CORPUS) -o $(FEATURE)

arff:
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q $(TRAINING_DISTRIB) -o $(ARFF)/$(TRAIN_ARFF)
	$(ANCOR2) arff no_assoc -i $(FEATURE) $(TEST_PARAMS) -o $(ARFF)/$(TEST_ARFF)

#	ancor2 model <weka-class-name> -t <arff-training-file>	[-T <arff-test-file>]
#							[weka-optional-params] -d <MODEL-model-filename>
gen-model:
	trainArff=$(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1)
	testArff=$(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1)
	$(ANCOR2) model $(ALGO_CLASS) \
		-t $(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1) \
		-T $(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1) -o -d $(MODEL)/$(ALGO).model

expe-feature:
	$(ANCOR2)  feature p $(CORPUS) -o $(FEATURE)
	mkdir $(FEATURE)/train
	-mv $(FEATURE)/aa_fichiers/004*.aa $(FEATURE)/train/

expe-arff:
	$(ANCOR2) arff no_assoc -i $(FEATURE)/train -q $(TRAINING_DISTRIB) -o $(ARFF)/$(TRAIN_ARFF)

expe-model: expe-arff
	$(ANCOR2) model $(ALGO_CLASS) \
		-t $(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1) \
		-no-cv -d $(MODEL)/$(ALGO).model

expe-scorer:
	-mkdir  $(CALLSCORER)/1
	-mkdir  $(CALLSCORER)/2
	-mkdir  $(CALLSCORER)/3

	$(ANCOR2) scorer no_assoc -i $(FEATURE)/aa_fichiers/00$(NUM_TEST)_C-1.aa -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/1 -m $(MODEL)/$(ALGO).model
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/aa_fichiers/00$(NUM_TEST)_C-2.aa -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/2 -m $(MODEL)/$(ALGO).model
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/aa_fichiers/00$(NUM_TEST)_C-3.aa -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/3 -m $(MODEL)/$(ALGO).model

run-scorer:
	$(ANCOR2) scorer no_assoc -i $(FEATURE) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER) -m $(MODEL)/$(ALGO).model


remove-extracted-Ancor:
	-$(RM) -rf $(ROOT)/Ancor
	-unlink Ancor

extract-Ancor: remove-extracted-Ancor
	-mkdir log
	-ln -s $(CORPUS_SRC)
	-unzip $(CORPUS_ZIP) -d $(CORPUS_SRC) 2>log/err.txt 1>log/MODEL.txt

gen-Ancor-Small: extract-Ancor
	mkdir -p $(ANCOR_SMALL)/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ac_fichiers
	-cp $(ANCOR_SMALL_SELECT_AA) $(ANCOR_SMALL)/aa_fichiers/
	-cp $(ANCOR_SMALL_SELECT_AC) $(ANCOR_SMALL)/ac_fichiers/

init-env: clean-all gen-Ancor-Small
	-mkdir -p $(GENERATED)
	-mkdir $(FEATURE)
	-mkdir $(ARFF)
	-mkdir $(MODEL)
	-mkdir $(CALLSCORER)
	-mkdir -p $(CORPUS)

classify:
	cp $(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1) $(CALLSCORER)/gold.arff
	cp $(shell find $(ARFF) -name $(TEST_ARFF)*.idff | head -1) $(CALLSCORER)/gold.idff
	$(ANCOR2) classify --model $(MODEL)/$(ALGO).model --in-arff  $(CALLSCORER)/gold.arff --out-arff $(CALLSCORER)/system.arff --force

chaining:
	$(ANCOR2) chaining --gold-arff $(CALLSCORER)/gold.arff --system-arff $(CALLSCORER)/system.arff --output-chains $(CALLSCORER)/out --force --csv
