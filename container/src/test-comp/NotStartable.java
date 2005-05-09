
public class NotStartable {

	public NotStartable(TestComp tc, StringBuffer sb) {
        sb.append("-NotStartable");
	    if (tc == null) {
			throw new NullPointerException();
		}
	}

	public void start() {}
	public void stop() {}
}

