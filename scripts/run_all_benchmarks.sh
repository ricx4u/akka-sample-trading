#!/bin/bash

RUN_SCRIPT=`dirname $0`/run_benchmark.sh
RESULT_DIR=`dirname $0`/../results

runTests() {
#  $RUN_SCRIPT org.samples.trading.basic.BasicPerformanceTest
#  $RUN_SCRIPT org.samples.trading.actor.ActorPerformanceTest
  $RUN_SCRIPT org.samples.trading.akka.AkkaPerformanceTest
  $RUN_SCRIPT org.samples.trading.actorbang.ActorBangPerformanceTest
  $RUN_SCRIPT org.samples.trading.akkabang.AkkaBangPerformanceTest
  $RUN_SCRIPT org.samples.trading.akkahawt.AkkaHawtPerformanceTest
}

# All tests with tx logging
#export BENCH_PROPS="-Dbenchmark.useTxLogFile=true -Dbenchmark=true -Dbenchmark.minClients=1 -Dbenchmark.maxClients=40 -Dbenchmark.useDummyOrderbook=false -Dbenchmark.resultDir=${RESULT_DIR}"
#runTests

# All tests without tx logging
#export BENCH_PROPS="-Dbenchmark.useTxLogFile=false -Dbenchmark=true -Dbenchmark.minClients=1 -Dbenchmark.maxClients=40 -Dbenchmark.useDummyOrderbook=false -Dbenchmark.resultDir=${RESULT_DIR}"
#runTests

# All tests without tx logging, with DummyOrderbook
export BENCH_PROPS="-Dbenchmark.useTxLogFile=false -Dbenchmark=true -Dbenchmark.minClients=1 -Dbenchmark.maxClients=40 -Dbenchmark.useDummyOrderbook=true -Dbenchmark.repeatFactor=300 -Dbenchmark.resultDir=${RESULT_DIR}" 
runTests

