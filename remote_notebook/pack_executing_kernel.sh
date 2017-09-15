#!/bin/bash


TMPDIR=executing_kernel_tmp
rm -rf $TMPDIR
mkdir -p $TMPDIR/executing_kernels/python
mkdir -p $TMPDIR/executing_kernels/r

# Python Executing Kernel
cp code/pyspark_kernel/kernel.json $TMPDIR/executing_kernels/python/
cp code/pyspark_kernel/kernel_init.py $TMPDIR/executing_kernels/python/
cp code/pyspark_kernel/executing_kernel.py $TMPDIR/executing_kernels/python/

# R Executing Kernel
cp code/sparkr_kernel/kernel.json $TMPDIR/executing_kernels/r/
cp code/sparkr_kernel/kernel_init.R $TMPDIR/executing_kernels/r/

cp code/*.py $TMPDIR/executing_kernels/
cp code/start.sh $TMPDIR

rm -f notebook_executing_kernel.zip
cd $TMPDIR
zip -r ../notebook_executing_kernel.zip *
cd ..
rm -rf $TMPDIR
