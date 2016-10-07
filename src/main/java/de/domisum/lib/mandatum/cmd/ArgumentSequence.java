package de.domisum.lib.mandatum.cmd;

import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Used to define the combinations of arguments that a command accepts.
 * <p>
 * <p>
 * Example: /heal [player] <health>
 * This command would accept the following argument sequences:
 * - new ArgumentSequence("withPlayer", Player.class, Double.class)
 * - new ArgumentSequence("onSelf", Double.class)
 * <p>
 * When a command executes, the name of the selected ArgumentSequence will be accessible
 * in the String "argumentSequenceName" in the command object.
 * <p>
 * Example: The player enters "/heal 10"
 * Then the argumentSequenceName in the Command object will be "onSelf".
 * This String can now be used to read the arguments properly.
 */
public class ArgumentSequence
{

	// PROPERTIES
	private String name;
	/**
	 * The classes of the arguments, in order.
	 */
	private List<Class<?>> argumentClasses;


	// -------
	// CONSTRUCTOR
	// -------

	/**
	 * Constructs an ArgumentSequence with the provided name and provided argument classes.
	 *
	 * @param name            The name of the ArgumentSequence
	 * @param argumentClasses The classes of the Arguments
	 */
	@APIUsage
	public ArgumentSequence(String name, Class<?>... argumentClasses)
	{
		this(name, new ArrayList<>(Arrays.asList(argumentClasses)));
	}

	/**
	 * @see #ArgumentSequence(String, Class[]) ()
	 */
	private ArgumentSequence(String name, List<Class<?>> argumentClasses)
	{
		this.name = name;
		this.argumentClasses = argumentClasses;
	}


	// -------
	// GETTERS
	// -------

	/**
	 * @return The name of the ArgumentSequence
	 */
	public String getName()
	{
		return this.name;
	}


	// -------
	// VALIDATION
	// -------

	/**
	 * Checks if the length of the supplied arguments fits the arguments specified in this Object.
	 * If the last argument of this ArgumentSequence is ArgumentMessage, true is returned if the number
	 * of supplied arguments is equal or greater than the number of arguments of the ArgumentSequence,
	 * including the ArgumentMessage.
	 * This means that the ArgumentMessage has to be at least one word long.
	 *
	 * @param args the arguments to check
	 * @return true if the number of supplied arguments fits this ArgumentSequence, false otherwise
	 */
	public boolean doesArgumentLengthFit(List<String> args)
	{
		// message at the end of the command
		if(this.argumentClasses.size() > 0)
			if(this.argumentClasses.get(this.argumentClasses.size()-1) == ArgumentMessage.class)
				// message has to be at leas one long
				return args.size() >= this.argumentClasses.size();

		return args.size() == this.argumentClasses.size();
	}

	/**
	 * Checks if the provided arguments fit the set arguments of this object. If this is not the case, an error message is
	 * returned to be displayed to the command dispatcher. If no error message is returned, the arguments are valid.
	 *
	 * @param args the arguments of the command
	 * @return the error message
	 */
	public String validate(List<String> args)
	{
		// loop over argumentClasses instead of args to handle messages (arguments will be longer than argument classes)
		for(int i = 0; i < this.argumentClasses.size(); i++)
		{
			Class<?> clazz = this.argumentClasses.get(i);
			String arg = args.get(i);

			String errorMessage = validate(clazz, arg);
			if(errorMessage != null)
				return errorMessage;
		}

		return null;
	}

	private String validate(Class<?> clazz, String arg)
	{
		// this returns "" when the argument is just straight out wrong. The message will be replaced by the usage message in the
		// command executor before being sent to the player

		// always true
		if(clazz == ArgumentMessage.class)
			return null;
		else if(clazz == String.class)
			return null;

		else if(clazz == Integer.class)
		{
			if(!arg.matches("^-?[0-9]+$"))
				return "";
		}

		else if((clazz == Double.class) || (clazz == Float.class))
		{
			if(!arg.matches("^-?[0-9]+(\\.[0-9]+)*$"))
				return "";
		}

		else if(clazz == Boolean.class)
		{
			if(!(arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")))
				return "";
		}

		else if(clazz == Player.class)
		{
			Player player = Bukkit.getPlayer(arg);
			if(player == null)
				return "The player with the name '"+arg+"' isn't currenly online ";
		}

		else if(clazz == UUID.class)
		{
			if(!arg.toLowerCase().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))
				return "The uuid '"+arg+"' is invalid";
		}
		else
			throw new RuntimeException("The class '"+clazz.getName()+"' has not been added to the validation method");

		return null;
	}


	// -------
	// MESSAGE
	// -------

	/**
	 * Currently only supported as the last argument for clarity purposes.
	 */
	@APIUsage
	public class ArgumentMessage
	{

	}

}
