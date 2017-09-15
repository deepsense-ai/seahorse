#!/usr/bin/env python
# Copyright 2017 deepsense.ai (CodiLime, Inc)
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


"""
    Generator of Scala code for DOperations and DMethods.
    Used to generate files in graph library:
      * ai.deepsense.deeplang.DOperations
      * ai.deepsense.deeplang.DMethods
"""

import sys
import textwrap

operable_class = "DOperable"
knowledge_class = "DKnowledge"
parameters_name = "parameters"
infer_context_class = "InferContext"
infer_context_name = "context"
exec_context_class = "ExecutionContext"
exec_context_name = "context"
warnings_class = "InferenceWarnings"
in_prefix = "TI_"
out_prefix = "TO_"
default_inference_warnings = "{}.empty".format(warnings_class)
typetag_fields_prefix = "tTag"

def bounded_parameters(number, prefix, variance):
  return ["{}{}{} <: {} : ru.TypeTag".format(variance, prefix, i, operable_class)
          for i in xrange(number)]

def unbounded_parameters(number, prefix):
  return ["{}{} <: {}".format(prefix, i, operable_class)
          for i in xrange(number)]

def typetag_fields(number, prefix):
  return ["def {}{}{}: ru.TypeTag[{}{}]".format(typetag_fields_prefix, prefix, i, prefix, i)
          for i in xrange(number)]

def return_signature_from_list(l):
  if len(l) == 0: return "Unit"
  if len(l) == 1: return l[0]
  return "(" + ", ".join(l) + ")"

def one_per_line(l, tab_length=10, separator=","):
  if len(l) == 0: return ""
  tab = "\n" + tab_length * " "
  lines = (separator + tab).join(l)
  return tab + lines

def exec_arguments(number):
  return ["t{i}: {pref}{i}".format(i=i, pref=in_prefix)
          for i in xrange(number)]

def infer_arguments(number):
  return ["k{i}: {knowledge_class}[{pref}{i}]"
          .format(knowledge_class=knowledge_class, i=i, pref=in_prefix)
          for i in xrange(number)]

def infer_argument_names(number):
  return ", ".join(["k{i}".format(i=i) for i in xrange(number)])

def exec_return(number):
  return ["{pref}{i}".format(i=i, pref=out_prefix)
          for i in xrange(number)]

def infer_return(number):
  return ["{knowledge_class}[{pref}{i}]"
          .format(knowledge_class=knowledge_class, i=i, pref=out_prefix)
          for i in xrange(number)]

def knowledge_from_context(i, pass_type_tag):
  additional_arg = ""
  if pass_type_tag:
    additional_arg = "({tag_prefix}{pref}{i})"\
                     .format(pref=out_prefix, i=i, tag_prefix=typetag_fields_prefix)

  return ("{knowledge_class}({infer_context_name}" +\
            ".dOperableCatalog.concreteSubclassesInstances[{pref}{i}]{additional_arg})")\
            .format(knowledge_class=knowledge_class,
                    infer_context_name=infer_context_name,
                    pref=out_prefix, i=i, additional_arg=additional_arg)

def knowledge_tuple_from_context(length, pass_type_tag):
  if length == 0: return "()"
  knowledge = [knowledge_from_context(i, pass_type_tag) for i in xrange(length)]
  if length == 1: return "\n        " + knowledge[0]
  return "(" + one_per_line(knowledge, tab_length=8) + ")"

def default_infer_implementation(out_arity, pass_type_tag):
  return "({}, {})".format(
      knowledge_tuple_from_context(out_arity, pass_type_tag),
      default_inference_warnings)

def print_operation(in_arity, out_arity):
  operation_class = "DOperation"
  parameters_class = "DParameters"
  inner_exec = "execute"
  inner_infer = "inferKnowledge"
  arguments = "arguments"
  knowledge = "knowledge"

  generics_list = unbounded_parameters(in_arity, in_prefix) + \
                    unbounded_parameters(out_arity, out_prefix)
  casted_arguments = ["{args}({i}).asInstanceOf[{pref}{i}]"
                      .format(args=arguments, i=i, pref=in_prefix)
                      for i in xrange(in_arity)]
  casted_knowledge = ["{knowledge}({i}).asInstanceOf[{knowledge_class}[{pref}{i}]]"
                      .format(knowledge=knowledge,
                              knowledge_class=knowledge_class,
                              i=i,
                              pref=in_prefix)
                      for i in xrange(in_arity)]
  in_port_types = ["ru.typeTag[{pref}{i}]({tag_prefix}{pref}{i})"
                      .format(i=i, pref=in_prefix, tag_prefix=typetag_fields_prefix)
                      for i in xrange(in_arity)]
  out_port_types = ["ru.typeTag[{pref}{i}]({tag_prefix}{pref}{i})"
                      .format(i=i, pref=out_prefix, tag_prefix=typetag_fields_prefix)
                      for i in xrange(out_arity)]

  typetag_fields_list = typetag_fields(in_arity, in_prefix) + typetag_fields(out_arity, out_prefix)
  exec_arguments_list = exec_arguments(in_arity)
  infer_arguments_list = infer_arguments(in_arity)
  inferred_argument_names = infer_argument_names(in_arity)
  exec_return_list = exec_return(out_arity)
  infer_return_list = infer_return(out_arity)

  # For out_arity equal to 0, we need to return empty vector.
  # Otherwise, implicit conversions from Tuple to Vector will be used.
  maybe_empty_result = ""
  maybe_empty_infer_result = ""
  if out_arity == 0:
    maybe_empty_result = "\n        Vector()"
    maybe_empty_infer_result = "\n        (Vector(), {})".format(default_inference_warnings)

  print textwrap.dedent("""\
    abstract class {operation_class}{in_arity}To{out_arity}[{generics_list}]
      extends DOperation {{
      override final val inArity = {in_arity}
      override final val outArity = {out_arity}

      {typetag_fields}

      @transient
      override final lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector({in_port_types})

      @transient
      override final lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector({out_port_types})

      override final def executeUntyped({arguments}: Vector[{operable_class}])(
          {exec_context_name}: {exec_context_class}): Vector[{operable_class}] = {{
        {inner_exec}({casted_arguments})({exec_context_name}){maybe_empty_result}
      }}

      override final def inferKnowledgeUntyped({knowledge}: Vector[{knowledge_class}[{operable_class}]])(
          {infer_context_name}: {infer_context_class}): \
(Vector[{knowledge_class}[{operable_class}]], {warnings_class}) = {{
        {inner_infer}({casted_knowledge})({infer_context_name}){maybe_empty_infer_result}
      }}

      protected def {inner_exec}({exec_arguments})\
({exec_context_name}: {exec_context_class}): {exec_return}

      protected def {inner_infer}({infer_arguments})\
({infer_context_name}: {infer_context_class}): {infer_return} = {{ {infer_implementation}
      }}
    }}""".format(
      in_arity=in_arity,
      out_arity=out_arity,
      typetag_fields=one_per_line(typetag_fields_list, tab_length=6, separator="\n"),
      operable_class=operable_class,
      operation_class=operation_class,
      knowledge_class=knowledge_class,
      inner_exec=inner_exec,
      inner_infer=inner_infer,
      warnings_class=warnings_class,
      arguments=arguments,
      knowledge=knowledge,
      generics_list=one_per_line(generics_list, tab_length=8),
      casted_arguments=one_per_line(casted_arguments),
      casted_knowledge=one_per_line(casted_knowledge),
      exec_arguments=one_per_line(exec_arguments_list),
      infer_arguments=one_per_line(infer_arguments_list),
      inferred_argument_names=inferred_argument_names,
      exec_return=return_signature_from_list(exec_return_list),
      infer_return="({}, {})".format(
        return_signature_from_list(infer_return_list),
        warnings_class),
      maybe_empty_result=maybe_empty_result,
      maybe_empty_infer_result=maybe_empty_infer_result,
      exec_context_class=exec_context_class,
      exec_context_name=exec_context_name,
      infer_context_class=infer_context_class,
      infer_context_name=infer_context_name,
      infer_implementation=default_infer_implementation(out_arity, pass_type_tag=True),
      in_port_types=one_per_line(in_port_types, tab_length=8, separator=","),
      out_port_types=one_per_line(out_port_types, tab_length=8, separator=",")))

def print_method(in_arity, out_arity):
  parameters_type = "P"
  generics_list = [parameters_type] + bounded_parameters(in_arity, in_prefix, "") + \
                    bounded_parameters(out_arity, out_prefix, "+")
  apply_arguments_list = exec_arguments(in_arity)
  infer_arguments_list = infer_arguments(in_arity)
  apply_return_list = exec_return(out_arity)
  infer_return_list = infer_return(out_arity)
  inferred_argument_names = infer_argument_names(in_arity)

  print textwrap.dedent("""\
    abstract class {method_class}{in_arity}To{out_arity}[{generics_list}] extends {method_class} {{
      def apply({exec_context_name}: {exec_context_class})\
({parameters_name}: {parameters_type})
               ({apply_arguments}): {apply_return}

      def infer({infer_context_name}: {infer_context_class})\
({parameters_name}: {parameters_type})({infer_arguments}): {infer_return} = {{ {infer_implementation}
      }}
    }}""".format(
      in_arity=in_arity,
      out_arity=out_arity,
      method_class="DMethod",
      parameters_name="parameters",
      parameters_type=parameters_type,
      generics_list=one_per_line(generics_list, tab_length=8),
      apply_arguments=", ".join(apply_arguments_list),
      infer_arguments=one_per_line(infer_arguments_list),
      inferred_argument_names=inferred_argument_names,
      apply_return=return_signature_from_list(apply_return_list),
      infer_return="({}, {})".format(
        return_signature_from_list(infer_return_list),
        warnings_class),
      exec_context_class=exec_context_class,
      exec_context_name=exec_context_name,
      infer_context_class=infer_context_class,
      infer_context_name=infer_context_name,
      infer_implementation=default_infer_implementation(out_arity, pass_type_tag=False)))

def print_help_and_exit(command):
  sys.stderr.write("Usage: {} o|m\n".format(command))
  sys.exit(2)

def main(args):
  if len(args) != 2:
    print_help_and_exit(args[0])

  printers = {'o': print_operation, 'm': print_method}
  printer = printers.get(args[1])
  if not printer:
    print_help_and_exit()

  # Code below generates methods/operations from X to Y for (X,Y) <- [0;3]x[0;3].
  for i in xrange(4):
    for j in xrange(4):
      if ((i, j) != (0, 0)):
        printer(i, j)
        print

if __name__ == '__main__':
  main(sys.argv)
