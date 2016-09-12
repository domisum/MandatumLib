package de.domisum.lib.mandatum;

import java.util.logging.Logger;

import de.domisum.lib.mandatum.exe.MandatumCommandExecutor;
import de.domisum.lib.mandatum.exe.MandatumCommandRegisterer;
import org.bukkit.plugin.java.JavaPlugin;

public class MandatumLib
{

	// REFERENCES
	private static MandatumLib instance;
	private JavaPlugin plugin;

	protected MandatumCommandExecutor commandExecutor;


	// -------
	// CONSTRUCTOR
	// -------
	protected MandatumLib(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		new MandatumLib(plugin);
	}

	public static void disable()
	{
		if(instance == null)
			return;

		getInstance().onDisable();
		instance = null;
	}

	protected void onEnable()
	{
		this.commandExecutor = new MandatumCommandExecutor();

		getLogger().info(this.getClass().getSimpleName()+" has been enabled");
	}

	protected void onDisable()
	{
		getLogger().info(this.getClass().getSimpleName()+" has been disabled");
	}


	// -------
	// GETTERS
	// -------
	public static MandatumLib getInstance()
	{
		return instance;
	}

	public JavaPlugin getPlugin()
	{
		return this.plugin;
	}

	public static Logger getLogger()
	{
		return getInstance().plugin.getLogger();
	}


	public static MandatumCommandExecutor getCommandExecutor()
	{
		return getInstance().commandExecutor;
	}


	// -------
	// REGISTERING
	// -------
	public void registerCommands(String classPath)
	{
		MandatumCommandRegisterer registerer = new MandatumCommandRegisterer(classPath);
		this.commandExecutor.addCommandClasses(registerer.getCommandClasses());
	}

}
