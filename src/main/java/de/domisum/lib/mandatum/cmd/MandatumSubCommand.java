package de.domisum.lib.mandatum.cmd;

import de.domisum.lib.auxilium.util.java.annotations.API;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class MandatumSubCommand extends MandatumCommand
{


	// -------
	// CONSTRUCTOR
	// -------
	public MandatumSubCommand(CommandSender commandSender, List<String> args)
	{
		super(commandSender, args);
	}


	// -------
	// GETTERS
	// -------
	@API
	public abstract String getSuperCommandName();

}
