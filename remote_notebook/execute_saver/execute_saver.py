# Copyright (c) 2016, CodiLime Inc.

from nbconvert.preprocessors import Preprocessor

class ExecuteSaver(Preprocessor):

    def preprocess(self, nb, resources):
        resources["seahorse_notebook_content"] = nb

        return nb, resources
