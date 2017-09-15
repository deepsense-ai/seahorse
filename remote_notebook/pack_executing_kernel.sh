#!/bin/bash


TMPDIR=executing_kernel_tmp
rm -rf $TMPDIR
mkdir $TMPDIR
mkdir $TMPDIR/executing_kernel

cp code/executing_kernel/executing_kernel.py code/executing_kernel/kernel.json $TMPDIR/executing_kernel
cp code/*.py $TMPDIR/executing_kernel
cp code/start.sh $TMPDIR

rm -f notebook_executing_kernel.zip
cd $TMPDIR
zip -r ../notebook_executing_kernel.zip *
cd ..
rm -rf $TMPDIR
