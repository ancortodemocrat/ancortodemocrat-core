

###############################################################################
#					TABLE 6
###############################################################################

T6-gen_Small: extract-Ancor
	#ESLO_TRAIN
	mkdir -p $(ANCOR_SMALL)/ESLO_TRAIN/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ESLO_TRAIN/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_ESLO_apprentissage/aa_fichiers/* $(ANCOR_SMALL)/ESLO_TRAIN/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_ESLO_apprentissage/ac_fichiers/* $(ANCOR_SMALL)/ESLO_TRAIN/ac_fichiers/
	#OTG TRAIN
	mkdir -p $(ANCOR_SMALL)/OTG_TRAIN/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/OTG_TRAIN/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_OTG_apprentissage/aa_fichiers/* $(ANCOR_SMALL)/OTG_TRAIN/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_OTG_apprentissage/ac_fichiers/* $(ANCOR_SMALL)/OTG_TRAIN/ac_fichiers/
	#ESLO_TEST_1
	mkdir -p $(ANCOR_SMALL)/ESLO_TEST/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/ESLO_TEST/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_ESLO_test_$(NUM_TEST)/aa_fichiers/* $(ANCOR_SMALL)/ESLO_TEST/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_ESLO_test_$(NUM_TEST)/ac_fichiers/* $(ANCOR_SMALL)/ESLO_TEST/ac_fichiers/

	#OTG_TEST_1
	mkdir -p $(ANCOR_SMALL)/OTG_TEST/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/OTG_TEST/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_OTG_test_$(NUM_TEST)/aa_fichiers/* $(ANCOR_SMALL)/OTG_TEST/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_OTG_test_$(NUM_TEST)/ac_fichiers/* $(ANCOR_SMALL)/OTG_TEST/ac_fichiers/

	#UBS_TEST_1
	mkdir -p $(ANCOR_SMALL)/UBS_TEST/aa_fichiers
	mkdir -p $(ANCOR_SMALL)/UBS_TEST/ac_fichiers
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_UBS_test_$(NUM_TEST)/aa_fichiers/* $(ANCOR_SMALL)/UBS_TEST/aa_fichiers/
	-cp $(CORPUS_SRC)/Données_maj/Tableau6/corpus_UBS_test_$(NUM_TEST)/ac_fichiers/* $(ANCOR_SMALL)/UBS_TEST/ac_fichiers/



T6-init-env: clean-all T6-gen_Small
	-mkdir -p $(GENERATED)
	-mkdir $(FEATURE)
	-mkdir $(ARFF)
	-mkdir $(MODEL)
	-mkdir $(CALLSCORER)
	-mkdir -p $(CORPUS)

T6-init: T6-init-env
	-mkdir -p $(MODEL)/J48
	-mkdir $(MODEL)/SMO
	-mkdir $(MODEL)/NB
	-mkdir -p $(CALLSCORER)/$(ALGO)/OTG_UBS
	-mkdir $(CALLSCORER)/$(ALGO)/OTG_ESLO
	-mkdir $(CALLSCORER)/$(ALGO)/ESLO_UBS
	-mkdir $(CALLSCORER)/$(ALGO)/ESLO_OTG

T6-features:
	$(ANCOR2)  feature p $(ANCOR_SMALL)/ESLO_TRAIN -o $(FEATURE)/ESLO_TRAIN
	$(ANCOR2)  feature p $(ANCOR_SMALL)/OTG_TRAIN -o $(FEATURE)/OTG_TRAIN
	$(ANCOR2)  feature p $(ANCOR_SMALL)/ESLO_TEST -o $(FEATURE)/ESLO_TEST
	$(ANCOR2)  feature p $(ANCOR_SMALL)/OTG_TEST -o $(FEATURE)/OTG_TEST
	$(ANCOR2)  feature p $(ANCOR_SMALL)/UBS_TEST -o $(FEATURE)/UBS_TEST


T6-arff:
	$(ANCOR2) arff no_assoc -i $(FEATURE)/ESLO_TRAIN -q $(TRAINING_DISTRIB) -o $(ARFF)/ESLO_$(TRAIN_ARFF)
	$(ANCOR2) arff no_assoc -i $(FEATURE)/OTG_TRAIN -q $(TRAINING_DISTRIB) -o $(ARFF)/OTG_$(TRAIN_ARFF)
	$(ANCOR2) arff no_assoc -i $(FEATURE)/ESLO_TEST $(TEST_PARAMS) -o $(ARFF)/ESLO_$(TEST_ARFF)


ESLO_TRAIN=$(shell find $(ARFF) -name ESLO_$(TRAIN_ARFF)*.arff | head -1)
ESLO_TEST=$(shell find $(ARFF) -name ESLO_$(TEST_ARFF)*.arff | head -1)
OTG_TRAIN=$(shell find $(ARFF) -name OTG_$(TRAIN_ARFF)*.arff | head -1)

T6-model:
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(ESLO_TRAIN) -T $(ESLO_TEST) -d $(MODEL)/$(ALGO)/ESLO.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(OTG_TRAIN) -T $(ESLO_TEST) -d $(MODEL)/$(ALGO)/OTG.model

T6-prepare: T6-init T6-features T6-arff T6-model
	@echo ===========================================================
	@echo READY
	@echo ===========================================================

T6-clean-scorers: clean-cs
	-mkdir -p $(CALLSCORER)/$(ALGO)/OTG_UBS
	-mkdir $(CALLSCORER)/$(ALGO)/OTG_ESLO
	-mkdir $(CALLSCORER)/$(ALGO)/ESLO_UBS
	-mkdir $(CALLSCORER)/$(ALGO)/ESLO_OTG

T6-scorer:
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/UBS_TEST -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/OTG_UBS -m $(MODEL)/$(ALGO)/OTG.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ESLO_TEST -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/OTG_ESLO -m $(MODEL)/$(ALGO)/OTG.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/UBS_TEST -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/ESLO_UBS -m $(MODEL)/$(ALGO)/ESLO.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/OTG_TEST -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/ESLO_OTG -m $(MODEL)/$(ALGO)/ESLO.model --scorer $(SCORERS)
