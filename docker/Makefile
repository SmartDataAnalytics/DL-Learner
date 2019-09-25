current_dir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

default:
	docker build -t smartdataanalytics/dl-learner .

run:
	#docker run --rm --name dl-learner -v $(current_dir)results:/results -v $(current_dir)config:/config dl-learner
	docker run -it smartdataanalytics/dl-learner
run-example:
	#cp examples/father.conf.example examples/father.conf
	docker run -it smartdataanalytics/dl-learner bin/cli examples/father.conf