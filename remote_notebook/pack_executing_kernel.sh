#!/bin/bash
# Copyright 2017, deepsense.ai
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



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
