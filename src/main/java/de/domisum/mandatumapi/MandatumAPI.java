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
	protected MandatumAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		new MandatumAPI(plugin);
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
