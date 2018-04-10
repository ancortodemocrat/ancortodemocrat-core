JAR_F=./target/ancor2-0.0.1-SNAPSHOT.jar
ANCOR2=java -jar $(JAR_F)

ANCOR_SMALL=./Ancor/Small
CORPUS=$(ANCOR_SMALL)
CORPUS_NAME=Small
TRAIN_ARFF=train_$(CORPUS_NAME)
TEST_ARFF=test_$(CORPUS_NAME)

J48=weka.classifiers.trees.J48
ALGO=$(J48)

GENERATED=/tmp/generated
FEATURE=$(GENERATED)/feature
ARFF=$(GENERATED)/arff
MODEL=$(GENERATED)/model
CALLSCORER=$(GENERATED)/callscorer

TRAINING_DISTRIB=1000 1400
ANCOR_SMALL_SELECT=00[4-5]*

trainArff := $(shell find $(ARFF) -name $(TRAIN_ARFF)*.arff | head -1)
testArff := $(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1)

info:
	@echo CORPUS=$(CORPUS)
	@echo CORPUS_NAME=$(CORPUS_NAME)

clean-all:
	$(RM) $(GENERATED)

clean-cs:
	$(RM) -r $(CALLSCORER)/*

clean-arff: clean-model
	$(RM) -r $(ARFF)/*

clean-features: clean-arff
	$(RM) -r $(FEATURE)/*

clean-model: clean-cs
	$(RM) -r $(MODEL)/*

features:
	$(ANCOR2)  feature p $(CORPUS) -o $(FEATURE)/$(CORPUS_NAME)

arff:
	$(ANCOR2) arff no_assoc -i $(FEATURE)/$(CORPUS_NAME) -q $(TRAINING_DISTRIB) -o $(ARFF)/$(TRAIN_ARFF)
	$(ANCOR2) arff no_assoc -i $(FEATURE)/$(CORPUS_NAME) 												-o $(ARFF)/$(TEST_ARFF)

#	ancor2 model <weka-class-name> -t <arff-training-file>	[-T <arff-test-file>]
#							[weka-optional-params] -d <out-model-filename>
gen-model:
	@echo train=$(trainArff)
	@echo test=$(testArff)
	$(ANCOR2) model $(ALGO) -t $(trainArff) -T $(testArff) -d $(MODEL)/$(CORPUS_NAME).model

run-scorer:
	mkdir $(CALLSCORER)/$(CORPUS_NAME);
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/$(CORPUS_NAME) -q 1000 4000 \
		-o $(CALLSCORER)/$(CORPUS_NAME) -m $(MODEL)/$(CORPUS_NAME).model







remove-extracted-Ancor:
	-unlink Ancor
	-rm -rf /tmp/Ancor

extract-Ancor:
	-ln -s /tmp/Ancor
	-unzip Ancor-Centre-CC-BY-NC-SA.zip -d /tmp/Ancor 2>/dev/null 1>/dev/null
	mkdir -p $(ANCOR_SMALL)/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ac_fichiers
	-cp Ancor/corpus_ESLO/aa_fichiers/$(ANCOR_SMALL_SELECT) /tmp/Ancor/Small/aa_fichiers/
	-cp Ancor/corpus_ESLO/ac_fichiers/$(ANCOR_SMALL_SELECT) /tmp/Ancor/Small/ac_fichiers/

init-env: extract-Ancor
	mkdir $(GENERATED)
	mkdir $(FEATURE)
	mkdir $(ARFF)
	mkdir $(MODEL)
	mkdir $(CALLSCORER)
