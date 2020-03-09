# Structure of a Network Module

Each concrete Java class in this directory implements a specific kind of network module. A spiking neural network (*SNN*)
can combine different kinds of network modules and may contain multiple module instances of the same kind with
possibly different parameters.

Abstract classes are used to factor out common traits of multiple module kinds with similar parametrization
requirements.

## Constituent files for each module kind

The following three files together define all aspects of a module kind:

* A concrete `public` Java class in the package `de.unibi.hbp.ncc.lang.modules`. This class must be a direct
  or indirect subclass of the abstract class [`NetworkModule`](../NetworkModule.java)
  in the package `de.unibi.hbp.ncc.lang`.
  
  This class defines the user-configurable design time parameters of each instance, including the impact of these
  parameters on the instance structure, like number of input or output ports. In addition, this class specifies
  a short, internal name `resourceFileBaseName` for the module kind, which is used as the basename for the other
  two files.

* A graphical icon *`basename.png`* for the module. The icon must be a square PNG image, usually with alpha transparency, and
  is stored in the directory
  [ncc/resources/de/unibi/hbp/ncc/editor/images/lang](../../../../../../../resources/de/unibi/hbp/ncc/editor/images/lang)
  for visual language-specific icons. The recommended size for the icon is 256x256 pixels. A scaled-down version is
  used to represent the module kind in the editor palette and inside graph nodes for module instances.

* A [StringTemplate 4][ST4-Syntax] template group file *`basename.stg`* that defines two Python code fragments.
  The first fragment is usually the definition of a Python function or class that builds a module instance.
  The second fragment is invoked, possibly multiple times, to create each module instance.

[ST4-Syntax]: https://github.com/antlr/stringtemplate4/blob/master/doc/cheatsheet.md
