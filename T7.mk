T7-all: T7-prepare T7-scorer
	@echo ====================================================
	@echo ======================= DONE =======================
	@echo ====================================================

T7-gen_Small: extract-Ancor
	#ESLO_TRAIN
	mkdir -p $(ANCOR_SMALL)/ESLO_TRAIN/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ESLO_TRAIN/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/train_eslo/corpus_ESLO_apprentissage/aa_fichiers/* \
			$(ANCOR_SMALL)/ESLO_TRAIN/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/train_eslo/corpus_ESLO_apprentissage/ac_fichiers/* \
			$(ANCOR_SMALL)/ESLO_TRAIN/ac_fichiers/
	#OTG TRAIN
	mkdir -p $(ANCOR_SMALL)/OTG_TRAIN/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/OTG_TRAIN/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/train_otg/corpus_OTG_apprentissage/aa_fichiers/* \
			$(ANCOR_SMALL)/OTG_TRAIN/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/train_otg/corpus_OTG_apprentissage/ac_fichiers/* \
			$(ANCOR_SMALL)/OTG_TRAIN/ac_fichiers/

	#OTG TEST
	mkdir -p $(ANCOR_SMALL)/OTG_TEST_$(NUM_TEST)/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/OTG_TEST_$(NUM_TEST)/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/test_otg/test$(NUM_TEST)/corpus_OTG_test_$(NUM_TEST)/aa_fichiers/* \
			$(ANCOR_SMALL)/OTG_TEST_$(NUM_TEST)/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/test_otg/test$(NUM_TEST)/corpus_OTG_test_$(NUM_TEST)/ac_fichiers/* \
			$(ANCOR_SMALL)/OTG_TEST_$(NUM_TEST)/ac_fichiers/
	#UBS TEST
	mkdir -p $(ANCOR_SMALL)/UBS_TEST_$(NUM_TEST)/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/UBS_TEST_$(NUM_TEST)/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/test_ubs/test$(NUM_TEST)/corpus_UBS_test_$(NUM_TEST)/aa_fichiers/* \
			$(ANCOR_SMALL)/UBS_TEST_$(NUM_TEST)/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/test_ubs/test$(NUM_TEST)/corpus_UBS_test_$(NUM_TEST)/ac_fichiers/* \
			$(ANCOR_SMALL)/UBS_TEST_$(NUM_TEST)/ac_fichiers/
	#ESLO TEST
	mkdir -p $(ANCOR_SMALL)/ESLO_TEST_$(NUM_TEST)/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ESLO_TEST_$(NUM_TEST)/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/test_eslo/test$(NUM_TEST)/corpus_ESLO_test_$(NUM_TEST)/aa_fichiers/* \
			$(ANCOR_SMALL)/ESLO_TEST_$(NUM_TEST)/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau7/test_eslo/test$(NUM_TEST)/corpus_ESLO_test_$(NUM_TEST)/ac_fichiers/* \
			$(ANCOR_SMALL)/ESLO_TEST_$(NUM_TEST)/ac_fichiers/

T7-init-env: clean-all T7-gen_Small
	-mkdir -p $(GENERATED)
	-mkdir $(FEATURE)
	-mkdir $(ARFF)
	-mkdir $(MODEL)
	-mkdir $(CALLSCORER)
	-mkdir -p $(CORPUS)

T7-init: T7-init-env
	-mkdir $(MODEL)/$(ALGO)

T7-features:
	$(ANCOR2) feature p $(ANCOR_SMALL)/ESLO_TRAIN -o $(FEATURE)/ESLO_TRAIN
	$(ANCOR2) feature p $(ANCOR_SMALL)/OTG_TRAIN -o $(FEATURE)/OTG_TRAIN
	$(ANCOR2) feature p $(ANCOR_SMALL)/OTG_TEST_$(NUM_TEST) -o $(FEATURE)/OTG_TEST_$(NUM_TEST)
	$(ANCOR2) feature p $(ANCOR_SMALL)/UBS_TEST_$(NUM_TEST) -o $(FEATURE)/UBS_TEST_$(NUM_TEST)
	$(ANCOR2) feature p $(ANCOR_SMALL)/ESLO_TEST_$(NUM_TEST) -o $(FEATURE)/ESLO_TEST_$(NUM_TEST)

T7-arff:

	java -cp $(WEKA_JAR) weka.filters.unsupervised.attribute.Remove -R 9,10,27 \
	-i $(CORPUS_SRC)/Données_maj/Tableau7/medium_trainingSet.arff -o $(ARFF)/MEDIUM_$(TRAIN_ARFF).arff
	$(ANCOR2) arff no_assoc -i $(FEATURE)/ESLO_TRAIN -q $(TRAINING_DISTRIB) -o $(ARFF)/ESLO_$(TRAIN_ARFF).arff
	$(ANCOR2) arff no_assoc -i $(FEATURE)/OTG_TRAIN -q $(TRAINING_DISTRIB) -o $(ARFF)/OTG_$(TRAIN_ARFF).arff
	$(ANCOR2) arff no_assoc -i $(FEATURE)/ESLO_TEST_$(NUM_TEST) -q $(TRAINING_DISTRIB) -o $(ARFF)/ESLO_$(NUM_TEST)_$(TEST_ARFF).arff
	$(ANCOR2) arff no_assoc -i $(FEATURE)/OTG_TEST_$(NUM_TEST) -q $(TRAINING_DISTRIB) -o $(ARFF)/OTG_$(NUM_TEST)_$(TEST_ARFF).arff


ESLO_TRAIN=$(shell find $(ARFF) -name ESLO_$(TRAIN_ARFF)*.arff | head -1)
ESLO_TEST=$(shell find $(ARFF) -name ESLO_$(NUM_TEST)_$(TEST_ARFF)*.arff | head -1)

OTG_TRAIN=$(shell find $(ARFF) -name OTG_$(TRAIN_ARFF)*.arff | head -1)
OTG_TEST=$(shell find $(ARFF) -name OTG_$(NUM_TEST)_$(TEST_ARFF)*.arff | head -1)

MEDIUM_TRAIN=$(shell find $(ARFF) -name MEDIUM_$(TRAIN_ARFF)*.arff | head -1)

T7-info:
	@echo ESLO_TRAIN=$(ESLO_TRAIN)
	@echo ESLO_TEST=$(ESLO_TEST)
	@echo OTG_TRAIN=$(OTG_TRAIN)
	@echo OTG_TEST=$(OTG_TEST)
	@echo MEDIUM_TRAIN=$(MEDIUM_TRAIN)

T7-model:
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(ESLO_TRAIN) -T $(ESLO_TEST) -d $(MODEL)/$(ALGO)/ESLO.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(OTG_TRAIN) -T $(OTG_TEST) -d $(MODEL)/$(ALGO)/OTG.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(MEDIUM_TRAIN) -T $(ESLO_TEST) -d $(MODEL)/$(ALGO)/MEDIUM.model

T7-prepare: T7-init T7-features T7-arff T7-model
	@echo ===========================================================
	@echo READY
	@echo ===========================================================

T7-classify:
	cp $(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1) $(CALLSCORER)/gold.arff
	cp $(shell find $(ARFF) -name $(TEST_ARFF)*.idff | head -1) $(CALLSCORER)/gold.idff
	$(ANCOR2) classify --model $(MODEL)/$(ALGO).model --in-arff  $(CALLSCORER)/gold.arff --out-arff $(CALLSCORER)/system.arff --force

	$(ANCOR2) classify

T7-scorer: clean-cs
	-mkdir -p $(CALLSCORER)/$(ALGO)/MEDIUM_OTG
	-mkdir $(CALLSCORER)/$(ALGO)/MEDIUM_UBS
	-mkdir $(CALLSCORER)/$(ALGO)/MEDIUM_ESLO

	-mkdir $(CALLSCORER)/$(ALGO)/OTG_OTG
	-mkdir $(CALLSCORER)/$(ALGO)/OTG_UBS

	-mkdir $(CALLSCORER)/$(ALGO)/ESLO_ESLO

	$(ANCOR2) scorer no_assoc -i $(FEATURE)/OTG_TEST_$(NUM_TEST) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/MEDIUM_OTG -m $(MODEL)/$(ALGO)/MEDIUM.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/UBS_TEST_$(NUM_TEST) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/MEDIUM_UBS -m $(MODEL)/$(ALGO)/MEDIUM.model --scorer $(SCORERS)

	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ESLO_TEST_$(NUM_TEST) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/MEDIUM_ESLO -m $(MODEL)/$(ALGO)/MEDIUM.model --scorer $(SCORERS)

	$(ANCOR2) scorer no_assoc -i $(FEATURE)/OTG_TEST_$(NUM_TEST) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/OTG_OTG -m $(MODEL)/$(ALGO)/OTG.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/UBS_TEST_$(NUM_TEST) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/OTG_UBS -m $(MODEL)/$(ALGO)/OTG.model --scorer $(SCORERS)

	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ESLO_TEST_$(NUM_TEST) -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/ESLO_ESLO -m $(MODEL)/$(ALGO)/ESLO.model --scorer $(SCORERS)
