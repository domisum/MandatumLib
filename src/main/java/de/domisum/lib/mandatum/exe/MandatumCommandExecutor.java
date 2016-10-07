package de.domisum.lib.mandatum.exe;

import de.domisum.lib.mandatum.cmd.ArgumentSequence;
import de.domisum.lib.mandatum.cmd.MandatumCommand;
import de.domisum.lib.mandatum.cmd.MandatumSuperCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MandatumCommandExecutor implements CommandExecutor
{

	// REFERENCES
	private Map<String, Class<? extends MandatumCommand>> commandClasses = new HashMap<>();


	// -------
	// CONSTRUCTOR
	// -------
	public MandatumCommandExecutor()
	{

	}


	// -------
	// CHANGERS
	// -------
	public void addCommandClasses(Map<String, Class<? extends MandatumCommand>> newCommandClasses)
	{
		// to lowercase so the the map can be searched by the lowercase commandname
		for(Entry<String, Class<? extends MandatumCommand>> entry : newCommandClasses.entrySet())
			this.commandClasses.put(entry.getKey().toLowerCase(), entry.getValue());
	}


	// -------
	// COMMAND
	// -------
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Class<? extends MandatumCommand> commandClazz = this.commandClasses.get(command.getName().toLowerCase());
		runCommand(commandClazz, sender, args);

		return true;
	}


	public void runCommand(Class<? extends MandatumCommand> commandClazz, CommandSender sender, List<String> args)
	{
		MandatumCommand command = getCommand(commandClazz, sender, args);

		if(!command.canBeRunByConsole() && (command.getSender() == null))
		{
			command.sendMessage("This command cannot be used by the console.");
			return;
		}

		permissionChecking:
		{
			// console
			if(command.getSender() == null)
				break permissionChecking;

			if(command.getSender().isOp())
				break permissionChecking;

			String permissionNode = command.getRequiredPermissionNode();
			if(permissionNode == null)
				break permissionChecking;

			if(command.getSender().hasPermission(permissionNode))
				break permissionChecking;

			command.sendMessage("You don't have permission to use this command.");
			return;
		}

		// don't check argument validity for supercommands, just pass them through
		if(command instanceof MandatumSuperCommand)
		{
			if(args.size() == 0)
			{
				command.sendUsageMessage();
				return;
			}

			command.execute();
			return;
		}

		// clone argumentSequences so the arraylist in the command class doesn't get altered
		List<ArgumentSequence> fittingArgumentSequences = new ArrayList<>(command.getArgumentSequences());
		fittingArgumentSequences.removeIf((as)->
		{
			// this takes messages in arguments into account
			return !as.doesArgumentLengthFit(args);
		});

		if(fittingArgumentSequences.size() == 0)
		{
			command.sendUsageMessage();
			return;
		}

		String highestPriorityValidationError = null;
		for(ArgumentSequence as : fittingArgumentSequences)
		{
			String validationError = as.validateArguments(args);
			if(highestPriorityValidationError == null)
				highestPriorityValidationError = validationError;

			// no errors
			if(validationError == null)
			{
				command.setArgumentSequenceName(as.getName());
				command.execute();
				return;
			}
		}

		// this is only called if none of the argumentsequences fit
		// this sends the error the highest priority argumentsequence gave to the user

		// the validator returns "" if the argument just doesn't fit, like a string instead of integer
		// so this means just return the standard error message
		if("".equals(highestPriorityValidationError))
			command.sendUsageMessage();
		else
			command.sendMessage(highestPriorityValidationError);
	}

	private void runCommand(Class<? extends MandatumCommand> commandClazz, CommandSender sender, String[] args)
	{
		runCommand(commandClazz, sender, new ArrayList<>(Arrays.asList(args)));
	}


	// -------
	// EXECUTION
	// -------
	private MandatumCommand getCommand(Class<? extends MandatumCommand> commandClazz, CommandSender sender, List<String> args)
	{
		Constructor<? extends MandatumCommand> constructor;
		MandatumCommand command = null;

		try
		{
			constructor = commandClazz.getConstructor(CommandSender.class, List.class);
			command = constructor.newInstance(sender, args);
		}
		catch(NoSuchMethodException|SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return command;
	}

}
