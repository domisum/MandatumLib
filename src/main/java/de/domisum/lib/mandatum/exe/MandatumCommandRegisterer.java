package de.domisum.lib.mandatum.exe;

import de.domisum.lib.auxilium.util.java.ClassUtil;
import de.domisum.lib.mandatum.MandatumLib;
import de.domisum.lib.mandatum.cmd.MandatumCommand;
import de.domisum.lib.mandatum.cmd.MandatumSubCommand;
import de.domisum.lib.mandatum.cmd.MandatumSuperCommand;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MandatumCommandRegisterer
{

	// PROPERTIES
	private String classPath;

	// REFERENCES
	private List<Class<? extends MandatumCommand>> commandClasses = new ArrayList<>();
	private Map<String, Class<? extends MandatumCommand>> commandsWithClasses = new HashMap<>();


	// -------
	// CONSTRUCTOR
	// -------
	public MandatumCommandRegisterer(String classPath)
	{
		this.classPath = classPath;

		register();
	}


	// -------
	// GETTER
	// -------
	public Map<String, Class<? extends MandatumCommand>> getCommandClasses()
	{
		return this.commandsWithClasses;
	}


	// -------
	// SCANNING
	// -------
	private void register()
	{
		MandatumLib.getLogger().info("Registering commands at '"+this.classPath+"' ...");

		// get all commands listed in plugin.yml
		Map<String, Map<String, Object>> commandsMap = MandatumLib.getInstance().getPlugin().getDescription().getCommands();
		if(commandsMap == null)
			throw new IllegalArgumentException("The plugin does not have any commands specified in the plugin.yml file");

		Set<String> pluginCommands = commandsMap.keySet();
		// the returned set is immutable, so just copy it
		pluginCommands = new HashSet<>(pluginCommands);

		// scan for the classes representing commands
		List<Class<?>> classes = ClassUtil.getClasses(this.classPath);
		if(classes == null)
			return;

		for(Class<?> clazz : classes)
			if(MandatumCommand.class.isAssignableFrom(clazz))
			{
				if(MandatumSubCommand.class.isAssignableFrom(clazz))
					continue;

				@SuppressWarnings("unchecked")
				Class<? extends MandatumCommand> commandClazz = (Class<? extends MandatumCommand>) clazz;
				this.commandClasses.add(commandClazz);
			}
			else
				MandatumLib.getLogger().warning(
						"Found class '"+clazz.getName()+"' in command package that isn't a command. It has been skipped");

		// loop through found classes and register each command
		for(Class<? extends MandatumCommand> cmdClazz : this.commandClasses)
		{
			String commandName = getCommandName(cmdClazz);

			// check if the command is also in the plugin.yml and remove it from the list, marking it as found
			if(!pluginCommands.contains(commandName))
			{
				MandatumLib.getLogger()
						.severe("The command '"+commandName+"' is represented by a class but not listed in the plugin.yml");

				continue;
			}
			pluginCommands.remove(commandName);

			registerCommand(cmdClazz, commandName);
		}

		// message about the commands that weren't present as classes
		for(String commandName : pluginCommands)
			MandatumLib.getLogger()
					.severe("The command '"+commandName+"' is present in the plugin.yml but wasn't represented as a class");

		MandatumLib.getLogger().info("Registering commands at '"+this.classPath+"' done");
	}

	private void registerCommand(Class<? extends MandatumCommand> commandClazz, String commandName)
	{
		// register command executor
		MandatumLib.getInstance().getPlugin().getCommand(commandName).setExecutor(MandatumLib.getCommandExecutor());

		if(MandatumSuperCommand.class.isAssignableFrom(commandClazz))
		{
			MandatumSuperCommand superCommand = (MandatumSuperCommand) getCommand(commandClazz);
			superCommand.registerSubCommands();
		}

		this.commandsWithClasses.put(commandName, commandClazz);
		MandatumLib.getLogger().info("The command '"+commandName+"' has been registered");
	}


	private static String getCommandName(Class<? extends MandatumCommand> commandClazz)
	{
		return getCommand(commandClazz).getName();
	}

	private static MandatumCommand getCommand(Class<? extends MandatumCommand> commandClazz)
	{
		MandatumCommand command = null;
		try
		{
			Constructor<?> constructor = commandClazz.getConstructor(CommandSender.class, List.class);
			command = (MandatumCommand) constructor.newInstance(null, null);
		}
		catch(InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e)
		{
			e.printStackTrace();
		}

		return command;
	}

}
