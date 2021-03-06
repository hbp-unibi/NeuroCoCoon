![NeuroCoCoon - Neuromorphic Computing CoCoon](assets/ncc_title_full.png)

# Adding a new Network Module

To define a new kind of network module, three components must be provided:

* a Java class that determines the user-configurable parameters and the related design time behavior of each module
  instance,
* an icon to depict the module kind and its instance in the visual language workbench,
* templates for PyNN-based Python code to create module instances when the network is simulated

## Java class

The Java class itself corresponds to the language concept of a network module kind. Objects (instances) of this class
are used to represent the module instances of this kind in the visual editor (one object per module instance).
This Java class must belong to the package [`de.unibi.hbp.ncc.lang.modules`](ncc/src/de/unibi/hbp/ncc/lang/modules)
and must be a direct or indirect subclass of the abstract class
[`NetworkModule`](ncc/src/de/unibi/hbp/ncc/lang/NetworkModule.java) in the package `de.unibi.hbp.ncc.lang`.
Intermediate (usually abstract) classes can be used to factor out common traits shared by multiple module kinds.

User-configurable design time parameters (*editable properties* in **NeuroCoCoon** terms) must be defined inside the
class as instance variables conforming to the interface type `de.unibi.hbp.ncc.lang.props.EditableProp<T>`, where the
generic type parameter `T` represents the data type of the property value. Typical examples with pre-supplied
implementations include properties that represent

* integral or floating-point values (data type `int` or `double`), possibly with a restricted range of values,
  like strictly positive values or probabilities in range `0.0` to `1.0` and an optional unit of measurement;

* references to other language entities, most notably a neuron type or a synapse type;

* a choice from a fixed set of predefined possibilities, similar to an enumeration type in textual languages;

* free-form entry of entity names or arbitrary short explanatory texts.

The following code is an excerpt from the [`WinnerTakeAll`](ncc/src/de/unibi/hbp/ncc/lang/modules/WinnerTakeAll.java)
module kind implementation. Default values for the properties appear as the last argument to the property
constructors. The parameter for the number of competing populations inside the winner-take-all architecture
determines the number of connection points (ports) of the module instance. Such structural impacts of changes to a
property value need to be declared on property creation. This causes the visual editor to track such changes and to
update the affected parts of the internal program representation. 

``` Java
private final IntegerProp numberOfPopulations, numberOfNeurons;
private final DoubleProp noiseWeight, noiseRate, noiseProbability, synapseDelay;

…

this.numberOfPopulations = new StrictlyPositiveIntegerProp("Number of Outcomes", this, 3)
    .addImpact(EditableProp.Impact.CELL_STRUCTURE);
this.numberOfNeurons = new StrictlyPositiveIntegerProp("Neurons per Population", this, 5);
this.noiseWeight = new NonNegativeDoubleProp("Noise Weight", this, 0.01);
this.noiseRate = new NonNegativeDoubleProp("Rate", this, 20.0).setUnit("Hz");
this.noiseProbability = new ProbabilityProp("Noise Probability", this, 0.7);
this.synapseDelay = new NonNegativeDoubleProp("Synapse Delay", this, 1.0).setUnit("ms");
``` 

To create a module instance by dragging an icon from the palette, the class must provide a factory object of type
`EntityCreator` that is parameterized with the type of the class itself. This factory object is, by convention, the
only instance of a private inner class named `Creator` and is available via a public read-only (`final`) class field
`CREATOR`. Beyond the actual module instance creation method `create`, the object provides a text caption and the base
filename for the icon and code generation templates.

Public getter methods make property values of a module instance available to the code generation templates. The
custom-defined Java annotation `@CodeGenUse` marks these methods, which have no apparent direct uses in the Java code
itself.   

## Icon

The graphical icon *`basename.png`* for the module must be a square PNG image, usually with alpha transparency, and
is stored in the directory
[ncc/resources/de/unibi/hbp/ncc/editor/images/lang](ncc/resources/de/unibi/hbp/ncc/editor/images/lang)
for visual language-specific icons. The recommended size for the icon is 256x256 pixels. A scaled-down version
represents the module kind in the editor palette and inside graph nodes for module instances.

![Example for a module icon at recommended size](ncc/resources/de/unibi/hbp/ncc/editor/images/lang/winner.png "Example for a module icon at recommended size 256x256")

**Icon Credits:** *winner* by *cindy clegane* from the *Noun Project*

## Python code generation templates

The [StringTemplate 4][ST4-Syntax] template group file *`basename.stg`* must define two Python code fragments.
The first fragment appears only once in the generated code and usually defines a Python function or class to build a
module instance.
The second fragment must create one module instance, usually by invoking the defined function or class, and may
appear multiple times.

A simplified and abbreviated excerpt from [`winner.stg`](ncc/src/de/unibi/hbp/ncc/lang/modules/winner.stg)
is given below. The [StringTemplate 4][ST4-Syntax]-Operator `::=` defines one template with the template name and
the names of the template parameters, if any, given on the left-hand side. Pairs of angled brackets
`<<` … `>>` delimit the body of longer templates, similar to the triple-quoted multi-line string literals in Python.
All text, whitespace and linebreaks inside such a template body are taken literally, except for template
expressions in single angled brackets `<` … `>`.

```
define_winner_builder() ::= <<
def build_winner_take_all(num_pops, num_neurons,
                          neuron_kind, neuron_params,
                          weight_noise, rate_noise, prob_noise, syn_delay):
    input_population_list, output_population_list = [], []
    
    # Create populations and excitatory connections
    …
    # Create cross-inhibition
    …
    # return internal populations that correspond to the externally visible input ports and output ports
    return { 'in': input_population_list, 'out': output_population_list }
>>

build_winner_instance(winner) ::= <<
build_winner_take_all(<winner.numberOfPopulations>, <winner.numberOfNeuronsPerPopulation>,
    <ref_neuronType(winner.neuronType)>,
    <winner.noiseWeight>, <winner.noiseRate>, <winner.noiseProbability>, <winner.synapseDelay>)
>>
```

The template `build_winner_instance` receives the Java representation of a specific module instance as its
parameter `winner` and uses the following two mechanisms to pass information about the module instance from the
visual editor to the Python code that creates the instance when the simulation is started.

1. For simple properties (numbers, text annotations) the code template invokes the public Java getter methods on the
   module instance in the visual editor, for example `<winner.numberOfPopulations>`. The returned values are passed
   unmodified to the Python code.

2. More complex properties use different representations on the Java and the [PyNN][PyNN]/Python side. Here, the editor
   provides auxiliary translation templates in its code generation framework
   [`python.stg`](ncc/resources/de/unibi/hbp/ncc/resources/python.stg).
   For example, the template invocation `<ref_neuronType(winner.neuronType)>` retrieves a reference to a user-specified
   neuron type from a property of the module instance, resolves this type reference to the type definition and expands
   it into two constituent items of information:
   * the fully qualified name `neuron_kind` of the [PyNN][PyNN] class which represents this kind of neuron
   * a Python dictionary `neuron_params` with values for all numeric parameters relevant for this kind of neuron.
   
   These two items of information are closely related and must be kept consistent. The visual language and its editor
   check and ensure this consistency by design, while in hand-written [PyNN][PyNN] code this would be the
   programmer's responsibility. Such expansions of a single concept into multiple related fragments of information
   are typical when translating from a higher-level domain-specific (visual) language into a lower-level
   implementation language with supporting libraries.

The created module instance must be returned from the Python function as a dictionary that uses the keys `in` and `out`
to refer to a list of the externally visible input or output populations (*ports* in the visual language terminology)
respectively. The order of the list elements needs to follow the ordering used by the Java method `getPortNames`
and the other port-related methods of the module instance on the visual editor side.

[ST4-Syntax]: https://github.com/antlr/stringtemplate4/blob/master/doc/cheatsheet.md
[PyNN]: https://neuralensemble.org/PyNN
