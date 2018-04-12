##	PARAMETERS
ALGO=J48
CORPUS_ZIP=./Ancor-Centre-CC-BY-NC-SA.zip
## /PARAMETERS

CORPUS_SRC=/tmp/Ancor

WEKA_CLASSIFIER=weka.classifiers
J48=trees.J48
SMO=functions.SMO
ALGO_CLASS=$(WEKA_CLASSIFIER).$($(ALGO))
TRAINING_DISTRIB=1000 1400
TEST_PARAMS= -q 1000 1400
SCORE_DISTRIB=100 400

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
ANCOR_SMALL_SELECT_AC=$(CORPUS_SRC)/corpus_ESLO/ac_fichiers/00[4-4]*
ANCOR_SMALL_SELECT_AA=$(CORPUS_SRC)/corpus_ESLO/aa_fichiers/00[4-4]*


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

features: clean-features
	$(ANCOR2)  feature p $(CORPUS) -o $(FEATURE)

arff: clean-arff
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q $(TRAINING_DISTRIB) -o $(ARFF)/$(TRAIN_ARFF)
	$(ANCOR2) arff no_assoc -i $(FEATURE) $(TEST_PARAMS)					-o $(ARFF)/$(TEST_ARFF)

#	ancor2 model <weka-class-name> -t <arff-training-file>	[-T <arff-test-file>]
#							[weka-optional-params] -d <out-model-filename>
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
	-unzip $(CORPUS_ZIP) -d $(CORPUS_SRC) 2>log/err.txt 1>log/out.txt

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
