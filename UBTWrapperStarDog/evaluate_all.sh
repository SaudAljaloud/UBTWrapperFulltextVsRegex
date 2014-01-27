#!/bin/sh

for i in 1 2 3 4 5; do
  sh evaluate.sh lucenesail_orig
  sh evaluate.sh lucenesail_hits_set
done

