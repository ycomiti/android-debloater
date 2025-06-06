package corsica.comiti.debloater.interfaces;

public interface ProcessListener {
	
	public void onOut(String out, Object... args);
	
	public void onErr(String err, Object... args);
	
	public void onExit(int code);
	
}
