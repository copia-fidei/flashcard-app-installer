import java.util.prefs.Preferences;

import static java.util.prefs.Preferences.userNodeForPackage;

public abstract class PageData {

    protected Preferences preferences = userNodeForPackage(this.getClass());

    protected PageDataPool pageDataPool;

    PageData(PageDataPool pageDataPool) {
        this.pageDataPool = pageDataPool;
	}

    public abstract void load();

	public abstract void save();

	public abstract ValidationResults validate();

    abstract void loadDefaults();
}
