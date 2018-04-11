JAR_F=./target/ancor2-0.0.1-SNAPSHOT.jar
ANCOR2=java -jar $(JAR_F)

GENERATED=/tmp/generated
FEATURE=$(GENERATED)/feature
ARFF=$(GENERATED)/arff
MODEL=$(GENERATED)/model
CALLSCORER=$(GENERATED)/callscorer

ANCOR_SMALL=/tmp/Ancor/Small
CORPUS_SRC=$(ANCOR_SMALL)
CORPUS=$(GENERATED)/chain/$(CORPUS_NAME)
CORPUS_NAME=Small
TRAIN_ARFF=train_$(CORPUS_NAME)
TEST_ARFF=test_$(CORPUS_NAME)

J48=weka.classifiers.trees.J48
ALGO=$(J48)

TRAINING_DISTRIB=1000 1400
ANCOR_SMALL_SELECT=00[4-4]*


all: init-env gen-corp features arff gen-model

info:
	@echo CORPUS=$(CORPUS)
	@echo CORPUS_NAME=$(CORPUS_NAME)

clean-all: clean-chain

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
	$(ANCOR2) chain $(CORPUS_SRC) -i $(CORPUS) > log/debug.txt

gen-corp:
	cp -r $(CORPUS_SRC)/* $(CORPUS)

features: clean-features
	$(ANCOR2)  feature p $(CORPUS) -o $(FEATURE)/$(CORPUS_NAME)

arff: clean-arff
	$(ANCOR2) arff no_assoc -i $(FEATURE)/$(CORPUS_NAME) -q $(TRAINING_DISTRIB) -o $(ARFF)/$(TRAIN_ARFF)
	$(ANCOR2) arff no_assoc -i $(FEATURE)/$(CORPUS_NAME) 												-o $(ARFF)/$(TEST_ARFF)

#	ancor2 model <weka-class-name> -t <arff-training-file>	[-T <arff-test-file>]
#							[weka-optional-params] -d <out-model-filename>
gen-model: clean-model
	trainArff=$(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1)
	testArff=$(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1)
	$(ANCOR2) model $(ALGO) \
		-t $(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1) \
		-T $(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1) -o -d $(MODEL)/$(CORPUS_NAME).model

run-scorer: clean-cs
	mkdir $(CALLSCORER)/$(CORPUS_NAME);
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/$(CORPUS_NAME) -q 1000 4000 \
		-o $(CALLSCORER)/$(CORPUS_NAME) -q 1000 1400 -m $(MODEL)/$(CORPUS_NAME).model







remove-extracted-Ancor:
	-$(RM) -rf /tmp/Ancor
	-unlink Ancor

extract-Ancor: remove-extracted-Ancor
	-ln -s /tmp/Ancor
	-unzip Ancor-Centre-CC-BY-NC-SA.zip -d /tmp/Ancor 2>log/err.txt 1>log/out.txt

gen-Ancor-Small: extract-Ancor
	mkdir -p $(ANCOR_SMALL)/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ac_fichiers
	-cp /tmp/Ancor/corpus_ESLO/aa_fichiers/$(ANCOR_SMALL_SELECT) $(ANCOR_SMALL)/aa_fichiers/
	-cp /tmp/Ancor/corpus_ESLO/ac_fichiers/$(ANCOR_SMALL_SELECT) $(ANCOR_SMALL)/ac_fichiers/

init-env: clean-all gen-Ancor-Small
	-mkdir $(GENERATED)
	-mkdir $(FEATURE)
	-mkdir $(ARFF)
	-mkdir $(MODEL)
	-mkdir $(CALLSCORER)
	-mkdir -p $(CORPUS)
