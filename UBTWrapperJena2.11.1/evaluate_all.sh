#!/bin/sh

for i in 1 2 3 4 5; do
  sh evaluate.sh derbylucene
  sh evaluate.sh mysqllucene
  sh evaluate.sh sdb_derbylucene
  sh evaluate.sh sdb_h2lucene
  sh evaluate.sh tdblucene
done
