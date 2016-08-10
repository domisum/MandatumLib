package de.domisum.mandatumapi.exe;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.domisum.auxiliumapi.util.java.ClazzUtil;
import de.domisum.mandatumapi.MandatumAPI;
import de.domisum.mandatumapi.cmd.MandatumCommand;
import de.domisum.mandatumapi.cmd.MandatumSubCommand;
import de.domisum.mandatumapi.cmd.MandatumSuperCommand;

public class MandatumCommandRegisterer
{

	// PROPERTIES
	protected String classPath;

	// REFERENCES
	protected List<Class<? extends MandatumCommand>> commandClasses = new ArrayList<>();
	protected Map<String, Class<? extends MandatumCommand>> commandsWithClasses = new HashMap<>();


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
	protected void register()
	{
		MandatumAPI.getLogger().info("Registering commands at '"+this.classPath+"' ...");

		// get all commands listed in plugin.yml
		Set<String> pluginCommands = MandatumAPI.getInstance().getPlugin().getDescription().getCommands().keySet();
		// the returned set is immutable, so just copy it
		pluginCommands = new HashSet<>(pluginCommands);

		// scan for the classes representing commands
		List<Class<?>> classes = ClazzUtil.getClasses(this.classPath);
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
				MandatumAPI.getLogger().warning(
						"Found class '"+clazz.getName()+"' in command package that isn't a command. It has been skipped");

		// loop through found classes and register each command
		for(Class<? extends MandatumCommand> cmdClazz : this.commandClasses)
		{
			String commandName = getCommandName(cmdClazz);

			// check if the command is also in the plugin.yml and remove it from the list, marking it as found
			if(!pluginCommands.contains(commandName))
			{
				MandatumAPI.getLogger()
						.severe("The command '"+commandName+"' is represented by a class but not listed in the plugin.yml");

				continue;
			}
			pluginCommands.remove(commandName);

			registerCommand(cmdClazz, commandName);
		}

		// message about the commands that weren't present as classes
		for(String commandName : pluginCommands)
			MandatumAPI.getLogger()
					.severe("The command '"+commandName+"' is present in the plugin.yml but wasn't represented as a class");

		MandatumAPI.getLogger().info("Registering commands at '"+this.classPath+"' done");
	}

	protected void registerCommand(Class<? extends MandatumCommand> commandClazz, String commandName)
	{
		// register command executor
		MandatumAPI.getInstance().getPlugin().getCommand(commandName).setExecutor(MandatumAPI.getCommandExecutor());

		if(MandatumSuperCommand.class.isAssignableFrom(commandClazz))
		{
			MandatumSuperCommand superCommand = (MandatumSuperCommand) getCommand(commandClazz);
			superCommand.registerSubCommands();
		}

		this.commandsWithClasses.put(commandName, commandClazz);
		MandatumAPI.getLogger().info("The command '"+commandName+"' has been registered");
	}


	protected static String getCommandName(Class<? extends MandatumCommand> commandClazz)
	{
		return getCommand(commandClazz).getName();
	}

	protected static MandatumCommand getCommand(Class<? extends MandatumCommand> commandClazz)
	{
		MandatumCommand command = null;
		try
		{
			command = commandClazz.getConstructor().newInstance();
		}
		catch(InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e)
		{
			e.printStackTrace();
		}

		return command;
	}

}
