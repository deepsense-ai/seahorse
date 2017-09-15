#!/bin/bash


TMPDIR=executing_kernel_tmp
rm -rf $TMPDIR
mkdir $TMPDIR

cp code/executing_kernel/executing_kernel.py code/executing_kernel/kernel.json $TMPDIR
cp code/*.py $TMPDIR

rm notebook_executing_kernel.zip
zip -r -j notebook_executing_kernel.zip $TMPDIR/*

rm -rf $TMPDIR