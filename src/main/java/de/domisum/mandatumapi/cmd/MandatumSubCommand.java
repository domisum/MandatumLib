package de.domisum.mandatumapi.cmd;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class MandatumSubCommand extends MandatumCommand
{


	// -------
	// CONSTRUCTOR
	// -------
	public MandatumSubCommand()
	{

	}

	public MandatumSubCommand(CommandSender commandSender, List<String> args)
	{
		super(commandSender, args);
	}


	// -------
	// GETTERS
	// -------
	public abstract String getSuperCommandName();

}
