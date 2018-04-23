
T5-init: init-env
	-mkdir -p $(MODEL)/J48
	-mkdir $(MODEL)/SMO
	-mkdir $(MODEL)/NB
	-mkdir -p $(CALLSCORER)/J48/Small
	-mkdir $(CALLSCORER)/J48/Medium
	-mkdir $(CALLSCORER)/J48/Big

T5-arff:
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q 3000 2150 -o $(ARFF)/$(TRAIN_ARFF)_small
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q 3000 3834 -o $(ARFF)/$(TRAIN_ARFF)_medium
	$(ANCOR2) arff no_assoc -i $(FEATURE) -q 3000 5234 -o $(ARFF)/$(TRAIN_ARFF)_big
	$(ANCOR2) arff no_assoc -i $(FEATURE) $(TEST_PARAMS) -o $(ARFF)/$(TEST_ARFF)

SMALL=$(shell find $(ARFF) -name $(TRAIN_ARFF)_small*.arff | head -1)
MEDIUM=$(shell find $(ARFF) -name $(TRAIN_ARFF)_medium*.arff | head -1)
BIG=$(shell find $(ARFF) -name $(TRAIN_ARFF)_big*.arff | head -1)
TEST=$(shell find $(ARFF) -name $(TEST_ARFF)*.arff | head -1)

T5-info:
	@echo $(SMALL)
	@echo $(MEDIUM)
	@echo $(BIG)
	@echo $(TEST)

T5-model:
	# J48
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(J48) -t $(SMALL) -T $(TEST) -d $(MODEL)/J48/small.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(J48) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/J48/medium.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(J48) -t $(BIG) -T $(TEST) -d $(MODEL)/J48/big.model

	# SMO
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(SMALL) -T $(TEST) -d $(MODEL)/SMO/small.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/SMO/medium.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(BIG) -T $(TEST) -d $(MODEL)/SMO/big.model
	 # NaivesBayes
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(SMALL) -T $(TEST) -d $(MODEL)/NB/small.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/NB/medium.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(BIG) -T $(TEST) -d $(MODEL)/NB/big.model

T5-scorer:
	#J48
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/J48/Small -m $(MODEL)/J48/small.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/J48/Medium -m $(MODEL)/J48/medium.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/J48/Big -m $(MODEL)/J48/big.model --scorer $(SCORERS)

T5-prepare-all: T5-init gen-corp features T5-arff T5-model
	@echo ===========================================================
	@echo READY
	@echo ===========================================================

T5-clean-cs:
	$(RM) $(MODEL)/J48/*
