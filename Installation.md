![NeuroCoCoon - Neuromorphic Computing CoCoon](assets/ncc_title_full.png)

# Installing NeuroCoCoon

## Building

The repository includes a project definition for the [Intellij IDEA][IntelliJ] development environment by
JetBrains s.r.o. Building this project (menu selection **Build**>**Build Project**) leaves a build artifact
`ncc.jar` in the directory [`web`](web). This Java archive file bundles all code and resources to run NeuroCoCoon
as a local Java application and forms the starting point for deployment as a web application inside the
[HBP collaboratory][HBP-Collab].

The project definition refers to the [CheerpJ][CheerpJ] interfacing library in its standard installation location.
This library is required for brwoser integration, when running as a web application. You can adjust the path to this
external library in the sub-category **Libraries** of the **Project Settings** in the **Project Structure**
configuration dialog.

## Installing as a local application

You need to have the Java Development Kit version 8 (JDK 8) installed on your machine. The Java Runtime Environment
version 8 (JRE 8) should be sufficient for running the application, but this has not been tested extensively.
Later Java versions, just as the current long term support (LTS) release Java 11 may work, but have undergone only
limited testing.

The following releases of Java 8 JDK are available for download:

* [Official release by Oracle][Oracle]  
  Please note and obey the current license terms.
* [OpenJDK release][OpenJDK]
* [Community release by Azul Systems][Azul]

You can either build the Java archive `ncc.jar` yourself, as described above, or download the
[latest release][NCC-Release] of the pre-built all-in-one runnable Java archive file `ncc-v1.0.0.jar`,
where the last part of the filename reflects the version number.

To start the editor from the command line, use a command of the form

`java -jar ncc-v1.0.0.jar`  
or  
`java -jar ncc-1.0.0.jar network.ncc`

The first command starts the visual editor with an empty network definition, whereas
the second variant instructs the editor to open the given input file at launch.
Some example files can be found in the directory [`examples`](examples).

If there are problems, check that the `java` command refers to a working installation of Java 8: `java -version`
must print some text including `java version "1.8.0_nnn"` where `nnn` stands for the bug fix release number.

## Prerequisites for running NEST simulations locally

When using the local application, the spiking neural networks can be simulated with the [NEST][NEST] software simulator.
This is the recommended approach to develop and test networks before scaling them up and bringing them onto the
remote neuromorphic hardware platforms [SpiNNaker and BrainScalesS][HBP-NMC].

You need to have [Python 3][Python3] installed. Then you should create an isolated virtual environment
(strongly recommended), where [NEST][NEST], [PyNN][PyNN], and some additional Python packages are installed.

The following terminal commands were used on macOS 10.14.6 to set up such an environment and demonstrate the
general approach.

```shell script
# use HomeBrew to install any optional native libraries for NEST that you want to use
brew install gsl

# create and activate the virtual environment in a sub-directory of the current directory
virtualenv ve_nest_218
source ve_nest_218/bin/activate

# install the Python SNN abstraction layer PyNN and packages used by the NEST Python bindings
pip3 install PyNN
# Cython is required for building the PyNEST bindings
pip3 install cython
# these packages are required by several installcheck PyNEST tests
pip3 install nose scipy matplotlib

# unpack NEST distribution inside the virtual environment directory
cd ve_nest_218
tar xfvz path/to/downloaded/nest-simulator-2.18.0.tar.gz
cd nest-simulator-2.18.0

# configure and build NEST
mkdir build; cd build
cmake -DCMAKE_INSTALL_PREFIX:PATH=/absolute/path/to/ve_nest_218 -Dwith-openmp=OFF ..
make -j

# install NEST inside the virtual directory 
make install
make installcheck
```

You may also build NEST with OpenMP and MPI enabled, but if there are any configuration or build problems with these
optional features, the easiest way out for our small-scale development and testing use of NEST is to disable them.
Note that the final installation check will then skip several test cases that depend on these features.
 
## Installing the collaboratory web app

The web application is built from the `ncc.jar` Java application archive and relies on [CheerpJ][CheerpJ]
by Leaning Technologies for purely client-side deployment in a web browser without any plugin or support for
Java applets installed. The directory [`web`](web) includes a suitable HTML page with all required Javascript
libraries to integrate the web applicaiton into the [HBP collaboratory][HBP-Collab]. The [`Makefile`](web/Makefile)
can build the necessary Javascript variant `ncc.jar.js` of the Java `ncc.jar` file, using the CheerpJ tool chain.
For deployment both files, `ncc.jar.js` and `ncc.jar` must be present on the server. Optionally, the `ncc.jar` file
can be replaced by a size-reduced version (CheerpJ supports either a *stripped* or a *packed* JAR file) that contains
just the required meta-information but omits the actual Java byte code. 

When running as a web application, NeuroCoCoon relies on the standard login and authorization mechanism of the
[HBP collaboratory][HBP-Collab] to provide access to the [SpiNNaker and BrainScalesS][HBP-NMC] remote platforms
for large-scale neuromorphic simulations.

Due to restrictions on [Cross-Origin Resource Sharing][CORS] in the collaboratory and in the software stacks of
the remote platforms, some features of NeuroCoCoon are limited in the web app. For example, thumbnail previews of
result plots cannot be provided inside the web app. Simulation jobs submitted to the queueing mechanism of a remote
platform take at least about 2:30 minutes to execute. This minimal overhead applies to short running simulations
when the job queue of the target platform is otherwise empty.

[NCC-Release]: https://github.com/hbp-unibi/NeuroCoCoon/releases
[IntelliJ]: https://www.jetbrains.com/idea/
[OpenJDK]: https://openjdk.java.net/install/
[Oracle]: https://www.oracle.com/java/technologies/javase-downloads.html
[Azul]: https://www.azul.com/downloads/zulu-community/
[CheerpJ]: https://www.leaningtech.com/pages/cheerpj.html
[Python3]: https://www.python.org
[PyNN]: https://neuralensemble.org/PyNN
[NEST]: https://www.nest-initiative.org
[HBP-NMC]: https://www.humanbrainproject.eu/en/silicon-brains/neuromorphic-computing-platform/
[HBP-Collab]: https://collab.humanbrainproject.eu/
[CORS]: https://www.w3.org/TR/cors/

