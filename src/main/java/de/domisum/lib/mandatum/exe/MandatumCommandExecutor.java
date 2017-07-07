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
import java.util.Objects;

public class MandatumCommandExecutor implements CommandExecutor
{

	// REFERENCES
	private Map<String, Class<? extends MandatumCommand>> commandClasses = new HashMap<>();


	// CHANGERS
	public void addCommandClasses(Map<String, Class<? extends MandatumCommand>> newCommandClasses)
	{
		// to lowercase so the the map can be searched by the lowercase commandname
		for(Entry<String, Class<? extends MandatumCommand>> entry : newCommandClasses.entrySet())
			this.commandClasses.put(entry.getKey().toLowerCase(), entry.getValue());
	}


	// COMMAND
	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Class<? extends MandatumCommand> commandClazz = this.commandClasses.get(command.getName().toLowerCase());
		runCommand(commandClazz, sender, args);

		return true;
	}


	private void runCommand(Class<? extends MandatumCommand> commandClazz, CommandSender sender, String[] args)
	{
		runCommand(commandClazz, sender, new ArrayList<>(Arrays.asList(args)));
	}

	public void runCommand(Class<? extends MandatumCommand> commandClazz, CommandSender sender, List<String> args)
	{
		MandatumCommand command = getCommand(commandClazz, sender, args);

		if(!command.canBeRunByConsole() && command.getSender() == null)
		{
			command.sendMessage("This command cannot be used by the console.");
			return;
		}

		boolean hasPermission = checkCommandPermission(command);
		if(!hasPermission)
			return;

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


		// check if number of args fit the command
		List<ArgumentSequence> fittingLengthArgumentSequences = getFittingLengthArgumentSequences(command, args);
		if(fittingLengthArgumentSequences.size() == 0)
		{
			command.sendUsageMessage();
			return;
		}

		// now check if the args validation gives some errors
		String highestPriorityArgumentSequenceError = null;
		for(ArgumentSequence as : fittingLengthArgumentSequences)
		{
			String validationError = as.validateArguments(args);
			if(highestPriorityArgumentSequenceError == null)
				highestPriorityArgumentSequenceError = validationError;

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
		if(Objects.equals(highestPriorityArgumentSequenceError, ""))
			command.sendUsageMessage();
		else
			command.sendMessage(highestPriorityArgumentSequenceError);
	}

	private static boolean checkCommandPermission(MandatumCommand command)
	{
		// console
		if(command.getSender() == null)
			return true;

		if(command.getSender().isOp())
			return true;

		String permissionNode = command.getRequiredPermissionNode();
		if(permissionNode == null)
			return true;

		if(command.getSender().hasPermission(permissionNode))
			return true;

		command.sendMessage("You don't have permission to use this command.");
		return false;
	}

	private static List<ArgumentSequence> getFittingLengthArgumentSequences(MandatumCommand command, List<String> args)
	{
		// clone argumentSequences so the ArrayList in the command class doesn't get changed
		List<ArgumentSequence> fittingArgumentSequences = new ArrayList<>(command.getArgumentSequences());
		fittingArgumentSequences.removeIf((as)->
		{
			// this takes messages in arguments into account
			return !as.doesArgumentLengthFit(args);
		});

		return fittingArgumentSequences;
	}


	// EXECUTION
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
