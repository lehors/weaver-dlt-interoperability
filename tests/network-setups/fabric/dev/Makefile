FABRIC_VERSION=2.1.0
FABRIC_CA_VERSION=1.4.7
FABRIC_COUCH_VERSION=0.4.20
CHAINCODE_NAME?=simplestate

.PHONY:	all
all: start

.PHONY: start
start:	start-network1 start-network2


.PHONY: start-interop
start-interop:	start-interop-network1 start-interop-network2


.PHONY: start-network1
start-network1:	.fabric-setup
	./network.sh up createChannel -ca -nw network1
	./network.sh deployCC -ch simplestate -nw network1	

.PHONY:	start-network2
start-network2: .fabric-setup
	./network.sh up createChannel -ca -nw network2
	./network.sh deployCC -ch simplestate -nw network2	

.PHONY: start-interop-network1
start-interop-network1:	start-network1 setup-interop-network1

.PHONY:	start-interop-network2
start-interop-network2: start-network2 setup-interop-network2

.PHONY: setup-interop-network1
setup-interop-network1: setup-interop-cc
	./network.sh deployCC -ch interop -nw network1


.PHONY:	setup-interop-network2
setup-interop-network2: setup-interop-cc
	./network.sh deployCC -ch interop -nw network2

.PHONY: setup-interop-cc
setup-interop-cc:
	./scripts/setupCCLocal.sh

.fabric-setup: 
	curl -sSL https://bit.ly/2ysbOFE | bash -s -- $(FABRIC_VERSION) $(FABRIC_CA_VERSION) $(FABRIC_COUCH_VERSION) -s	
	touch .fabric-setup

.PHONY:	stop
stop:	stop-network1 stop-network2

.PHONY: stop-network1
stop-network1: 
	./network.sh down -nw network1

.PHONY: stop-network2
stop-network2:
	./network.sh down -nw network2

.PHONY:	remove
remove:	remove-network1 remove-network2

.PHONY: remove-network1
remove-network1: 
	./network.sh clean -nw network1

.PHONY: remove-network2
remove-network2:
	./network.sh clean -nw network2

.PHONY: clean
clean:	stop
	+rm -rf bin
	+rm -rf config
	+rm .fabric-setup
