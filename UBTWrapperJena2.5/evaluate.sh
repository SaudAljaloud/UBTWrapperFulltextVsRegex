#!/bin/sh

for i in 1 2 3 4 5 6 7 8 9 10; do
  if [ -z `ls querylog.$1.$i.*.txt 2> /dev/null` ]; then
    echo "evaluating $1 run #$i";
    ./query_ubt_$1.sh > querylog.$1.$i.`date +%Y%m%d-%H%M%S`.txt;
    exit 0;
  fi
done

