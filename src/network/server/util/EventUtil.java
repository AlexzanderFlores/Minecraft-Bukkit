package network.server.util;

import network.Network;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventUtil {
	private static List<Listener> listeners = new ArrayList<Listener>();

	public static boolean isListener(Listener listener) {
		return listeners.contains(listener);
	}

	public static boolean isListener(Listener listener, Event event) {
		for(RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
			if(registeredListener.getListener() == listener) {
				return true;
			}
		}

		return false;
	}

	public static void register(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, Network.getInstance());
		listeners.add(listener);
	}

	public static void unregister(Listener listener) {
		try {
			for(Method method : listener.getClass().getMethods()) {
				if(method.getAnnotation(EventHandler.class) != null) {
					unregisterEvent((Class<? extends Event>)method.getParameterTypes()[0], listener);
				}
			}
			listeners.remove(listener);
		}
		catch(Exception e){}
	}

	static void unregisterEvent(Class<? extends Event> eventClass, Listener listener) {
		for(RegisteredListener regListener : HandlerList.getRegisteredListeners(Network.getInstance())) {
			if(regListener.getListener() == listener) {
				try {
					((HandlerList)eventClass.getMethod("getHandlerList").invoke(null)).unregister(regListener);
				}
				catch(Exception e){}
			}
		}
	}
}
