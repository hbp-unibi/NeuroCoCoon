![NeuroCoCoon - Neuromorphic Computing CoCoon](assets/ncc_title_full.png)

# Overview

**NeuroCoCoon** is a **protective** development environment and experimentation **workbench** for
spiking neural networks (*SNN*s) running on **neuromorphic hardware**.

The network is described in a graph-based visual language and is built from **neuron populations** (nodes) and **synapse
connectors** (edges). User-defined **neuron types** and **synapse types** ensure consistent parameter choices for all
related parts of the network architecture. In addition, predefined **network modules** can be used as architectural
building blocks with external connection points (ports).

Networks can be simulated on the [NEST][NEST] software simulator, if NeuroCoCoon is running as a local application,
or on the [SpiNNaker and BrainScalesS][HBP-NMC] platforms of the European Horizon 2020 [Human Brain Project][HBP] (*HBP*),
if NeuroCoCoon is running as a client side web application inside the [HBP collaboratory][HBP-Collab].

# Editor Window

![Major parts of editor window](assets/editor_window_anatomy.png "Major parts of editor window")

The editor window consists of the following major parts:

1. **Toolbar** with buttons for the most important operations, some editor settings, and the
   simulation control elements. See the next section for more details.
   
2. **Palette** with basic elements and modules that can be added to the spiking neural network graph.

3. **Outline** view with a scaled-down overview of the full network graph.

4. **Main view** of the graph at a configurable scale. This will often only show a part of the full
   network, as indicated by the blue rectangle in the outline view.
   
5. **Details area** with several tabs that are used to display and edit different aspects of the
   network that are not directly visible or accessible in the visual representation of the graph in the main view.
 
## Toolbar

![Regions of editor toolbar](assets/editor_toolbar_anatomy.png "Regions of editor toolbar")

The toolbar at the top of the editor window comprises these distinct regions:

1. Buttons for operations at the document level. These correspond to menu items in the **File** menu.
   For the web application editor document files are stored in a simulated file system on the client
   machine. The native storage format (file extension `.ncc`) uses an XML-based representation of the
   graph that is annotated with all parameters of the network architecture. As an alternative, a
   Portable Network Graphics raster image (file extension `.png`) of the graph can be saved. This file
   embeds the aforementioned XML representation of the network and can be opened later for further editing.

2. Buttons for standard editing operations known from drawing applications and other editors in general.
   These correspond to menu items in the **Edit** menu.
   
3. Buttons to *undo* the last editing operation(s) or to *redo* previously undone operations.
   These operations are also available in the **Edit** menu.
   
4. Popup menu and input box to set the desired scale of the graph in the main viewing area below.

5. Popup menu of all *synapse types* defined in the current network architecture. This determines
   the parameters and settings used by new synapse connections added to the graph. The details area
   at the right of the window is used to define synapse types and to edit the parameters of existing
   synapses afterwards.
   
6. Buttons to check the current network architecture for structural and semantic errors and to run a
   simulation of the network. A network can only be simulated if it contains no errors, but warnings
   and information items concerning likely unintended parameter settings do not prevent simulation.
   
7. Popup menu to select the target platform for executing the simulation. Unavailable platforms are
   greyed out, i.e., the [SpiNNaker and BrainScalesS][HBP-NMC] platforms, if running as a local application,
   or the [NEST][NEST] software simulator, if running as a web application. Always available is the
   **Source only** platform, which does not actually execute the simulation, but just shows the generated
   [Python 3][Python3] [PyNN][PyNN] code for inspection.

8. Status summary display that tracks the progress and the general success or failure of checking a network
   architecture or running a simulation.
   This is complemented by longer messages in the status bar at the bottom of the editor window, by
   message dialog boxes, or by information in the **Results** tab of the detail area as needed.

## Editing the Network

Creating a network architecture in the NeuroCoCoon visual editor consists of the following general steps.

* Adding neuron populations or network modules to the graph by dragging them from the palette into the main view.
* Drawing synapse connections between the populations or the connection points (*ports*) of the modules.
* Attaching data plots with probe connections that monitor network behaviour of interest.
* Adjusting parameters of the currently selected network item in the **Inspector** tab of the details area.
* Checking the network architecture for errors and finally running a simulation of the network.

### Creating Neuron Populations and Module Instances

Neuron populations and spike sources are added as nodes to the network graph by dragging them from the **Basic** tab
of the palette into the main view. Their symbol in the palette is a miniature version of the visual representation
for the node in the graph.

![Basic network elements and modules in the palette](assets/palette_tabs.png "Basic network elements and modules in the palette")

The **Modules** tab of the palette contains one icon for each kind of module in the module library. A module instance
is added to the network graph by dragging the icon from the palette into the main view. Module instances appear in the
graph as pale violet rounded boxes that show the icon representing the module kind. Darker violet triangles represent
the input port and output ports of the module instance. These ports serve as connection points and the triangles point
towards the direction of the information flow, i.e., into the modular sub-network contained in the module instance or
out of that sub-network. 

![Module instances in the graph](assets/module_instances.png "Module instances in the graph")

The above figure shows on the left two module instances of the kind *Synfire Chain* where the length of the chain
has been set to three and seven stages respectively in the **Inspector** tab of the details area.
On the right there are two module instances of the kind *Winner-Take-All*, both set to select one of three possible
outcomes. However, the right instance has been flipped into a vertical flow direction (menu **Shape**>**Flip Module**)
with input port along the top border and output port along the bottom border, instead of there default location along
the left and right border respectively. This is a purely cosmetic choice to get a better general layout of the
visual network architecture. Similarly, the box for a module instance my be resized graphically to get a more
appropriate spacing between ports.

The drawing area for the network graph automatically extends to the right or to the bottom, if you drag any node so that
it extends beyond the current boundaries.

### Adding Synapses

Synapses are the most prevalent kind of edges in the network graph. Such an edge between two neuron populations usually
represent many connections between individual neurons inside those collections. The *synapse type* determines the
systematic strategy that is used to apply this conceptual high-level connection between populations at the neuron
level (the *connector*) and quantitative parameters of each such neuron connection, like the weight and the delay.
The synapse type for newly added synapses is determined by the corresponding popup menu in the the toolbar.
User-defined synapse types are created and managed in the *Synapses* tab of the details area.

There are the following three alternative ways to add synapses (and connecting edges in general) to the network graph.

#### Drawing Synapses

The usually most convenient way is to drag with the left mouse button from the center of the source node to the
center of the target node, as shown below. A green outline around the node indicates a mouse pointer location
close enough to the center of the node where starting to drag will create an edge. Similar behaviour applies to the
target node. A red outline indicates a node that is an invalid target and will not accept the pending connection.
Nodes that do not allow any kind of incoming or outgoing connections show no colored border at all.

![Drawing a synapse from the center of a graph node](assets/drawing_synapse.png "Drawing a synapse from the center of a graph node")

By default you can also temporarily create dangling edges that are not yet attached to a target node. This allows you
to create especially very long edges in multiple steps. However, all such dangling edges must be connected to valid
nodes before the network can be simulated.

If you want to select a node for editing, moving, resizing, or to inspect its parameters, you must click outside the
center area of the node. This causes the usual dashed selection marker with resizing handles to appear.

#### Using Connect Mode

This mode can be turned on and off in the menu **Options**>**Connections**>**Connect Mode**. While this mode is active
(menu item ticked) a small green square marks the exact center of the node under the mouse pointer and drawing an
edge can only be started from there. With connect mode turned off the active center area of a node is much larger.

![Starting to draw a synapse in connect mode](assets/connectmode_synapse_start.png "Starting to draw a synapse in connect mode")

Contrary to its name, connect mode makes it slightly harder to create connections, but simplifies normal node selection
and editing. This is especially relevant, if you want to visually move node labels:
menu **Options**>**Labels**>**Move Node Labels**, turned off by default.

#### Dragging from the Palette

Finally, edges can be added to the graph, just like nodes, by dragging the desired kind of edge from the **Basic** tab
of the palette. After dropping the edge into the main view the two endpoints must the be dragged in separate steps to
attach them to graph nodes.

![Adding a synapse by dragging from the palette](assets/dragging_synapse.png "Adding a synapse by dragging from the palette")

In the lower part of the above figure, the blue source handle of the synapse edge has already been attached to the
population on the left and the blue target handle is just about to be dropped onto the right population. The green
rectangle indicates that this connection will then be created, as its is structurally valid. The thin green line
provides a preview, how this connection will be routed.

Dragging edges from the palette is mainly useful, if you want to manually pick a specific kind of edge, .e.g., a
synapse connection versus a probe connection. Drawing synapses directly between nodes picks the most likely kind of
edge, if any valid kind exists, based on the source and target nodes involved.

### Plotting Data

Simulation results are provided in the form of plots that monitor spiking behaviour or continuous state variables of
selected neurons over the course of the simulation. Which neuron populations to monitor and what data to collect is
also represented visually in the graph of the network.

to create a plot composed of one or more panels, drag a **Data Plot** node from the palette into the main view.
This node needs to be connected to neuron populations or ports of network module instances by drawing **Data Probe**
connections. A probe starts at the data plot node and ends at the target node to be monitored, as if the data plot
node were measuring device. The probe connection is annotated with the kind of data to record and collect from the
target. In addition, a probe can be limited to just one sub-range of the neurons in the target node, because it is
often not practical to monitor all individual neurons of a population.

After running a simulation of the network small thumbnail images of all data plots are shown in the **Results** tab
of the details area. Double-clicking a thumbnail opens the full-sized plot in a separate window. Due to
security concerns in the HBP collaboratory, cross-origin resource sharing is disabled for the plot images created by
a remote simulation. Thus, the webb app cannot display thumbnail images with the actual plots. Selecting such a
placeholder image displays the full-sized plot image at the bottom of the web page, below the virtual screen of the
web app. 

By default, a data plot consists of one separate panel per monitored data kind. The same kind of data collected from
multiple target populations is shown as differently colored curves or dots inside this panel. You can reconfigure the
data plot node in the **Inspector** tab to use a separate panel per probe. This helps to distinguish otherwise
overlapping curves or spike train dots from multiple populations.


### Mouse Operations

The main view supports the following operations by using the mouse in conjunction with combinations of modifier keys.

* **Left click:** select a graph node or an edge for further editing.
* **Left double click:** select a node or edge and show its properties in the **Inspector** tab of the details area
* **Left drag:** move a selected node or draw an edge from the center of a node;
  dragging from the empty background area outside any node or edge creates a rectangular selection marquee that selects
  all completely enclosed items
* **Right click:** opens a context menu with the most important operations for the clicked item
* **\<Shift> + left click:** add the clicked item to the current selection or remove an already selected item from the
  current selection
* **\<Alt> + left drag:** creates a copy of the dragged item at the target position instead of just moving the
  existing item
* **\<Shift> + left drag:** limits movement of the dragged item to strictly horizontal or vertical
* **\<Ctrl> + left drag:** temporarily disables the grid while dragging an item
* **\<Ctrl> + \<Shift> + left drag:** pans (moves) the drawing area inside the main view
* **\<Alt> + \<Shift> + left drag:** always creates a rectangular selection marquee, even when starting the drag over
  an existing item (instead of the empty background)
* **\<Alt> + left double click:** cycles through the available routing schemes for clicked edge. This has purely a
  visual effect on the appearance of the graph. The name of the selected routing scheme is shown in the status bar
  at the bottom of the editor window. Several of the routing schemes provide additional draggable control points along
  the edge to influence the routing manually

**Usage on macOS:** The listed modifier keys apply to the local application on the Linux/Unix and Windows platforms,
as well as to the web application. On macOS the \<Option> key replaces the \<Alt> key and the
\<Command> (or \<Cmd>) key is used instead of \<Ctrl>. This matches the general conventions for
modifier keys on that platform. The \<Ctrl> key is used on macOS only to simulate the right
mouse button with input devices that offer just a single button.

### Important Menu Operations

The editor provides the usual menu operations for managing editor documents and copying, cutting, deleting the currently
selected graph nodes with their connecting edges. In addition, there are two sets of menu operations that help you in
organising the network graph: a **grid** for tidy alignment of graph nodes and a mechanism to **group** related
sub-structures of the graph.

The grid is enabled or disabled by the menu item **View**>**Grid**. The spacing between the grid locations is set by
**Diagram**>**Grid**>**Grid Size…**. That submenu also offer options to configure the visual appearance of the grid.
When the grid is enabled newly created nodes and dragged existing nodes automatically snap to the closets grid location.
The grid is enabled and shown as small dots by default. You can temporarily disable the snapping to grid locations by
holding down the \<Ctrl> key while dragging an item. 

To combine the currently selected graph nodes and their connecting edges into a group invoke **Shape**>**Group**.
Such a group is shown as a grey rectangle with a partly transparent background that encloses all contained nodes.
A group can be collapsed into a small square to hide its inner nodes. It is also possible to edit the subgraph
inside a group in isolation (menu **Shape**>**Enter group**) to reduce visual clutter and focus on just a part of
a larger network. Such groups merely combine manually created nodes and edges of the graph. In contrast to network
module instances groups have no properties of their own and do not generate structures according to scalable
architectural principles.

Although not shown in the menus for technical reasons, the standard keyboard shortcuts are available for menu items.
Usage of the \<Ctrl> versus \<Command> modifier key depends on platform conventions, as explained for the mouse
operations above. 
 
## Inspector

The **Inspector** tab in the details area shows all properties of the currently selected graph node or edge in a table.
Values of properties can be edited, by clicking into the corresponding table cell and entering a new value or by
selecting from a popup menu with all possible choices. The latter applies, for example, to the neuron type of a
neuron population, as shown in the image below. Here, the popup menu lists all neuron types that have been defined
for the currently edited network.

![Editing the neuron type property of a neuron population node](assets/inspector_population.png "Editing the neuron type property of a neuron population node")

Pressing the \<Return>, \<Enter>, or \<Tab> key commits the property value in the currently focused cell and moves the
focus to the next property in the row below. Hold down \<Shift> to move upward instead. Obviously invalid entries,
like invalid characters inside a number, a negative neuron count, or an out-of-range probability must be corrected
before the value can be committed. More complex conditions that depend on multiple property values, possibly spread
across multiple elements of the network graph, are checked separately before the simulation can be started.

The narrow first column of the table marks properties with special behaviour. An arrow indicates a property that is
*not* defined in the selected graph item itself, but is part of the neuron or synapse type referenced by the item.
Such properties are shown in the table for convenience and can, in general, be edited in the same way as the item's
own properties. However, as neuron and synapse types are intended to share a consistent set of parameters across
multiple neuron populations of synapse connections, editing a property of such a type affects all elements of the
network that refer to this type. If this is not intended, you need to create a separate copy (a *duplicate*) of the
type and change the **Neuron Type** or **Synapse Type** property of the currently selected item to refer to the new
type.

![Inspecting a synapse connection with a pre-defined (left) or a user-defined (right) synapse type](assets/inspector_synapsetype.png "Inspecting a synapse connection with a pre-defined (left) or a user-defined (right) synapse type")

The double-circle symbol marks properties that cannot be modified, because they belong to a pre-defined neuron or
synapse type. The image above demonstrates this difference, where the synapse inspected in the left part references
a pre-defined synapse type. The properties of this synapse type are thus marked with an arrow (referenced property)
and the double-circle (read-only predefined value). The right part of the image shows a similar constellation with a
user-defined synapse type, where the referenced properties are editable.

## Neuron and Synapse Types

The concept of shared types for multiple neuron populations or synapses that play a similar role in the network
architecture supports experiments with a manageable scope for parameter exploration. While a type is referenced
by nearly every graph node and edge, the type definitions themselves have no direct visible representation in the
graph, as this would introduce too much visual clutter and obscure the network architecture.

Instead, the **Neurons** and **Synapses** tabs in the details area are used to manage and edit those types. Both tabs
share the same general structure of a master-detail editor. The table at the top lists all currently defined types
and provides buttons for creating a new type from scratch, duplicating the selected type, or deleting the selected
type. Types can be renamed by double-clicking their name. This is equivalent to editing the **Name** property of the
type. The double-circle marks pre-defined types, which can be neither renamed nor deleted, but they can be duplicated
as a starting point for a user-defined type.

![Master-detail editor with a pre-defined neuron type selected (left) and a user-defined neuron type selected (right)](assets/masterdetail_neurontype.png "Master-detail editor with a pre-defined neuron type selected (left) and a user-defined neuron type selected (right)")

The image above shows in the left part a pre-defined neuron type selected in the master table. The property details
table at the bottom thus has all properties marked as read-only. In the right part of the image a user-defined type
has been selected in the master table and the details table at the bottom can be used to change the properties of the
neuron type. Note that the set of neuron parameters shown in the details table depends on the selected **Neuron Kind**
near the top of the table. Inapplicable neuron parameters retain their values during an editing session, but revert to
their default values, when loading a network from disk.

The image below shows a similar constellation with synapse types, where the set of available properties depends on the
selected **Connector**. For example, the **Probability** property is not applicable to the simpler *one-to-one* or
*all-to-all* connectors.

![Master-detail editor with a pre-defined synapse type selected (left) and a user-defined synapse type selected (right)](assets/masterdetail_synapsetype.png "Master-detail editor with a pre-defined synapse type selected (left) and a user-defined synapse type selected (right)")

## Running a Simulation

To start a simulation run of the currently edited network, select the desired target platform from the popup menu in the
toolbar and click the **Run** button to the left of that menu. This first checks the network for structural
problems and invalid parameter constellations. These checks would be meaningless (or outright annoying) while the
network is still under construction and it is normal to have temporary inconsistencies. 

The image below shows, what happens when trying to simulate a network with such inconsistencies. The **Problems** tab
in the details area replaces the usual **Results** tab with data plots. Each row in that table represents one
error message (red stop sign) or a warning message (yellow triangle sign). The second column gives the name of the
graph node or edge that caused the message and the message itself appears in the third column. Selecting a table row
scrolls the affected graph node or edge into view and selects it in the main view. Every item in the graph with an
associated error or warning message is also marked with a small overlay symbol so that problem areas of the network
can be easily recognised in the main view.

![Diagnostics displayed when trying to run a network with an error and two warnings](assets/diagnostics.png "Diagnostics displayed when trying to run a network with an error and two warnings")

If the network contains no errors, the actual simulation is started. While the simulation runs, progress information is
shown in the status summary display at the right end of the toolbar. For the remote platforms, the status is updated
every 5 seconds and you should expect a minimum round-trip time of two or three minutes, even if your simulation job
makes it to the front of the work queue immediately.

When the simulation run finishes successfully, the **Results** tab is brought forward and gives you access to all
the data plots you have added to the network. 

TODO
* platform availability, Source only platform
* differences compared to explicit check action (info items)
* changing icon of Check toolbar button

---

This open source software code was developed in part in the Human Brain Project, funded from the
European Union’s Horizon 2020 Framework Programme for Research and Innovation under the
Specific Grant Agreement No. 720270 (HBP SGA1) and 785907 (HBP SGA2).

[NEST]: https://www.nest-initiative.org
[Python3]: https://www.python.org
[PyNN]: https://neuralensemble.org/PyNN
[HBP]: https://www.humanbrainproject.eu
[HBP-NMC]: https://www.humanbrainproject.eu/en/silicon-brains/neuromorphic-computing-platform/
[HBP-Collab]: https://collab.humanbrainproject.eu/