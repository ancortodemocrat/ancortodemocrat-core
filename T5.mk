
T5-init: init-env
	-mkdir -p $(MODEL)/$(ALGO)
	-mkdir $(MODEL)/SMO
	-mkdir $(MODEL)/NB
	-mkdir -p $(CALLSCORER)/$(ALGO)/Small
	-mkdir $(CALLSCORER)/$(ALGO)/Medium
	-mkdir $(CALLSCORER)/$(ALGO)/Big

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
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(SMALL) -T $(TEST) -d $(MODEL)/$(ALGO)/small.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/$(ALGO)/medium.model
	$(ANCOR2) model $(WEKA_CLASSIFIER).$($(ALGO)) -t $(BIG) -T $(TEST) -d $(MODEL)/$(ALGO)/big.model

	# # SMO
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(SMALL) -T $(TEST) -d $(MODEL)/SMO/small.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/SMO/medium.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(SMO) -t $(BIG) -T $(TEST) -d $(MODEL)/SMO/big.model
	#  # NaivesBayes
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(SMALL) -T $(TEST) -d $(MODEL)/NB/small.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(MEDIUM) -T $(TEST) -d $(MODEL)/NB/medium.model
	# $(ANCOR2) model $(WEKA_CLASSIFIER).$(NAIVES_BAYES) -t $(BIG) -T $(TEST) -d $(MODEL)/NB/big.model

T5-scorer:
	#J48
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/Small -m $(MODEL)/$(ALGO)/small.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/Medium -m $(MODEL)/$(ALGO)/medium.model --scorer $(SCORERS)
	$(ANCOR2) scorer no_assoc -i $(FEATURE)/ -q $(SCORE_DISTRIB) \
		-o $(CALLSCORER)/$(ALGO)/Big -m $(MODEL)/$(ALGO)/big.model --scorer $(SCORERS)

T5-prepare-all: T5-init gen-corp features T5-arff T5-model
	@echo ===========================================================
	@echo READY
	@echo ===========================================================

T5-clean-cs:
	$(RM) $(MODEL)/$(ALGO)/*
