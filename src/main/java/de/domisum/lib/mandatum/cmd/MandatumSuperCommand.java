package de.domisum.lib.mandatum.cmd;

import de.domisum.lib.auxilium.util.java.ClazzUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.mandatum.MandatumLib;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class MandatumSuperCommand extends MandatumCommand
{

	// -------
	// CONSTRUCTOR
	// -------
	public MandatumSuperCommand(CommandSender commandSender, List<String> args)
	{
		super(commandSender, args);
	}

	public abstract void registerSubCommands();


	// -------
	// GETTERS
	// -------
	@Override
	public String getUsage()
	{
		return "/"+getName()+" <subcommand> [arguments of subcommand]";
	}

	@Override
	public List<ArgumentSequence> getArgumentSequences()
	{
		return null;
	}

	@APIUsage
	public abstract String getSubCommandsClassPath();

	@SuppressWarnings("unchecked")
	protected Map<String, Class<? extends MandatumSubCommand>> findSubCommands()
	{
		MandatumLib.getLogger().info("Registering subcommands of '"+getName()+"' ...");

		List<Class<?>> classes = ClazzUtil.getClasses(getSubCommandsClassPath());
		Map<String, Class<? extends MandatumSubCommand>> subCommandClasses = new HashMap<>();
		for(Class<?> c : classes)
		{
			if(!MandatumSubCommand.class.isAssignableFrom(c))
			{
				MandatumLib.getLogger().warning(
						"Found class '"+c.getName()+"' in subcommand package that isn't a subcommand. It has been skipped");
				continue;
			}

			MandatumSubCommand subCommand = null;
			try
			{
				Constructor<?> constructor = c.getConstructor(CommandSender.class, List.class);
				subCommand = (MandatumSubCommand) constructor.newInstance(null, null);
			}
			catch(InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e)
			{
				e.printStackTrace();
			}

			if(!subCommand.getSuperCommandName().equalsIgnoreCase(getName()))
			{
				MandatumLib.getLogger()
						.severe("The subcommand '"+c.getName()+"' belongs to the command '"+subCommand.getSuperCommandName()
								+"', not '"+getName()+"'");
				continue;
			}

			subCommandClasses.put(subCommand.getName().toLowerCase(), (Class<? extends MandatumSubCommand>) c);
			MandatumLib.getLogger().info("Registered subcommands '"+subCommand.getName()+"'");
		}

		MandatumLib.getLogger().info("Registering subcommands of '"+getName()+"' done");

		return subCommandClasses;
	}


	public abstract Map<String, Class<? extends MandatumSubCommand>> getSubCommands();

	public abstract Class<? extends MandatumSubCommand> getSubCommandClass(String subCommandName);


	// -------
	// COMMUNICATION
	// -------
	@Override
	public void sendUsageMessage()
	{
		super.sendUsageMessage();
		sendMessage("These subcommands exist:");
		for(Entry<String, Class<? extends MandatumSubCommand>> entry : getSubCommands().entrySet())
			sendMessage(" - "+entry.getKey());
	}

	// -------
	// EXECUTION
	// -------
	@Override
	public void execute()
	{
		String subCommandName = this.args.get(0);
		Class<? extends MandatumSubCommand> subCommand = getSubCommandClass(subCommandName.toLowerCase());
		if(subCommand == null)
		{
			sendUsageMessage();
			return;
		}

		// the error handling is done in the shouldExecuteMethod itself
		if(!shouldExecute())
			return;

		List<String> subCommandArgs = new ArrayList<>(this.args);
		subCommandArgs.remove(0); // remove first arg since it is the name of the subcommand

		// run subcommand
		// if the command was sent by console sender is null, but this is not important since the sender of the subcommand will be
		// null again anyways, since instanceof can handle null
		MandatumLib.getCommandExecutor().runCommand(subCommand, this.sender, subCommandArgs);
	}

	/**
	 * Not 100% sure for what this method is used for, but I don't want to delete it yet
	 *
	 * @return if the subcommand should be executed
	 */
	@APIUsage
	protected abstract boolean shouldExecute();

}
