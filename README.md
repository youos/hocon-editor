Java tool to view and edit HOCON (.conf) files in a **TreeView** 

## About

This is a simple Java tool which bases on the [lightbend configuration library](https://github.com/lightbend/config)
which is used to read out configurations. 

It allows you to import a 
directory filled with configurations to view and edit.

The main advantage of this editor is that it notices the **complete** directory,
which means that even .jar files in your selected folders will be 
read if there are any configurations in there. 

If you import your configurations, it merges every single one together, ensuring that 
the **unique(!) application.conf** overwrites all existing sources.
Unique, because the editor only works as long as your selected sources contain only **one** application.conf

Once you have finished with selecting your folders, you can start to view and edit a merged configuration.
You can click through the TreeView on the left panel and get information to every tree item you click on
on the right panel.

#### This information contains:

- **Origin file path** 
- **Configuration key path**
- **Value Type**
- **Comments**
- **Value**
- **Environment Variable**




