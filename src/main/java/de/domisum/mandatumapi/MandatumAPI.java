package de.domisum.mandatumapi;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import de.domisum.mandatumapi.exe.MandatumCommandExecutor;
import de.domisum.mandatumapi.exe.MandatumCommandRegisterer;

public class MandatumAPI
{

	// REFERENCES
	private static MandatumAPI instance;
	private JavaPlugin plugin;

	protected MandatumCommandExecutor commandExecutor;


	// -------
	// CONSTRUCTOR
	// -------
	public MandatumAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public void onEnable()
	{
		this.commandExecutor = new MandatumCommandExecutor();

		getLogger().info(this.getClass().getSimpleName() + " has been enabled\n");
	}

	public void onDisable()
	{
		getLogger().info(this.getClass().getSimpleName() + " has been disabled\n");
	}


	// -------
	// GETTERS
	// -------
	public static MandatumAPI getInstance()
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
