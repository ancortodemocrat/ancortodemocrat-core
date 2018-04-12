##	PARAMETERS

ALGO=J48
CORPUS_ZIP=./Donnees_corpus.zip

ANCOR_SMALL_SELECT_AC=$(CORPUS_SRC)/Données_corpus/Tableau5/corpus_apprentissage/ac_fichiers/*
ANCOR_SMALL_SELECT_AA=$(CORPUS_SRC)/Données_corpus/Tableau5/corpus_apprentissage/aa_fichiers/*
# ANCOR_SMALL_SELECT_AC=$(CORPUS_SRC)/corpus_ESLO/ac_fichiers/00[4-4]*
# ANCOR_SMALL_SELECT_AA=$(CORPUS_SRC)/corpus_ESLO/aa_fichiers/00[4-4]*

## /PARAMETERS

CORPUS_SRC=/tmp/Ancor

WEKA_CLASSIFIER=weka.classifiers
J48=trees.J48
SMO=functions.SMO
NAIVES_BAYES=bayes.NaivesBayes
ALGO_CLASS=$(WEKA_CLASSIFIER).$($(ALGO))
TRAINING_DISTRIB=3000 2150
TEST_PARAMS= -q 2757 3861
SCORE_DISTRIB=275 786

JAR_F=./target/ancor2-0.0.1-SNAPSHOT.jar
ANCOR2=java -jar $(JAR_F)

CORPUS_NAME=Small
ANCOR_SMALL=$(CORPUS_SRC)/Small

GENERATED=/tmp/generated/$(CORPUS_NAME)
FEATURE=$(GENERATED)/feature
ARFF=$(GENERATED)/arff
MODEL=$(GENERATED)/model
CALLSCORER=$(GENERATED)/callscorer

SMALL_SRC=$(ANCOR_SMALL)
CORPUS=$(GENERATED)/chain/
TRAIN_ARFF=train
TEST_ARFF=test


all: init-env gen-corp features arff gen-model

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
	$(ANCOR2) arff no_assoc -i $(FEATURE) $(TEST_PARAMS)					-o $(ARFF)/$(TEST_ARFF)

#	ancor2 model <weka-class-name> -t <arff-training-file>	[-T <arff-test-file>]
#							[weka-optional-params] -d <MODEL-model-filename>
gen-model: clean-model
	trainArff=$(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1)
	testArff=$(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1)
	$(ANCOR2) model $(ALGO_CLASS) \
		-t $(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1) \
		-T $(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1) -o -d $(MODEL)/$(ALGO)

run-scorer: clean-cs
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/ -m $(MODEL)/Model


remove-extracted-Ancor:
	-$(RM) -rf /tmp/Ancor
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
	-mkdir $(GENERATED)
	-mkdir $(FEATURE)
	-mkdir $(ARFF)
	-mkdir $(MODEL)
	-mkdir $(CALLSCORER)
	-mkdir -p $(CORPUS)

T5-init:
	-mkdir -p $(MODEL)/J48
	-mkdir $(MODEL)/SMO
	-mkdir $(MODEL)/NB
	-mkdir -p $(CALLSCORER)/J48/Small
	-mkdir $(CALLSCORER)/J48/Medium
	-mkdir $(CALLSCORER)/J48/Big

T5-arff: clean-arff
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q 3000 2150 -o $(ARFF)/$(TRAIN_ARFF)_small
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q 3000 3834 -o $(ARFF)/$(TRAIN_ARFF)_medium
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q 3000 5234 -o $(ARFF)/$(TRAIN_ARFF)_big
	$(ANCOR2) arff no_assoc -i $(FEATURE) $(TEST_PARAMS)					-o $(ARFF)/$(TEST_ARFF)

SMALL=$(shell find $(ARFF) -name $(TRAIN_ARFF)_small*.arff | head -1)
MEDIUM=$(shell find $(ARFF) -name $(TRAIN_ARFF)_medium*.arff | head -1)
BIG=$(shell find $(ARFF) -name $(TRAIN_ARFF)_big*.arff | head -1)
TEST=$(shell find $(ARFF) -name $(TEST_ARFF)* | head -1)

T5-info:
	@echo $(SMALL)
	@echo $(MEDIUM)
	@echo $(BIG)

T5-model:
	# J48
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(J48) -t $(SMALL) -T $(TEST) -d $(MODEL)/J48/small.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(J48) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/J48/medium.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(J48) -t $(BIG) -T $(TEST) -d $(MODEL)/J48/big.model
	# SMO
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(SMALL) -T $(TEST) -d $(MODEL)/SMO/small.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/SMO/medium.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(BIG) -T $(TEST) -d $(MODEL)/SMO/big.model
	# # NaivesBayes
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(SMALL) -T $(TEST) -d $(MODEL)/NB/small.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/NB/medium.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(BIG) -T $(TEST) -d $(MODEL)/NB/big.model

T5-scorer:
	#J48
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/J48/Small -m $(MODEL)/J48/small.model
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/J48/Medium -m $(MODEL)/J48/medium.model
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/J48/Big -m $(MODEL)/J48/big.model
