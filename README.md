Java tool to view and edit HOCON (.conf) files in a **TreeView** 

## About

This is a simple Java tool which is based on a forked version of the [lightbend configuration library](https://github.com/lightbend/config)
that is used to read out configurations. 

It allows you to import a 
directory filled with configurations to view and edit.

The main advantage of this editor is that it notices the **complete** directory,
which means that even .jar files in your selected folders will be 
read if there are any configurations in there.

Another special thing is that the whole configuration is displayed in 
a **Java TreeView** where it's much easier to
recognize the structure of your configuration files.  

If you import your configurations, it merges every single one together, ensuring that 
the **unique(!) application.conf** overwrites all other existing sources.
Unique, because the editor only works as long as your selected sources contain only **one** application.conf

Once you have finished with selecting your folders, you can start to view and edit a merged configuration.
You can click through the TreeView on the left panel and get information to every tree item you click on
on the right panel.

#### This information contains:

- **Origin file path** 
- **Configuration key path**
- **Value type**
- **Comments**
- **Value**
- **Environment variable**

From these, the last 3 are editable, means, you can set your own comments (even multiline),
set another value for the selected key or set an environment variable for this key (adds ${variable} syntax).

The value type is automatically changed, based on your value input. 
So, if you type "true", it will be a String, but without "" it will be converted into a boolean.
The same happens with numbers of any kind.

Once you have done your changes, click on the "Save" button below. 
This will not yet edit the real files, but it's necessary before that step, 
because it changes the big merged configuration in the program.

Now if you click on "Apply Changes" in the toolbar on top, All changes you made
will be written into the unique **application.conf**. Other configurations will not be changed!


## Other features

- Renaming keys
- Deleting keys
- Adding keys

## Installation

There is a published executable jar file in the project root.




