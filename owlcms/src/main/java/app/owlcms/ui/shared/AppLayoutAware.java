package app.owlcms.ui.shared;

import com.github.appreciated.app.layout.behaviour.AbstractLeftAppLayoutBase;

public interface AppLayoutAware {
	/**
	 * A Vaadin RouterLayout contains an instance of an AppLayout.
	 * 
	 * A RouterLayout is referenced as a layout by some Content, meaning that the content will
	 * be inserted inside and laid out (i.e. displayed).  OwlcmsRouterLayout delegates to an AppLayout
	 * which actually does the layouting.  AppLayout is a Java API to the Google app-layout web component.
	 * 
	 * @return the RouterLayout which is the target of the Vaadin Flow Route
	 */
	public OwlcmsRouterLayout getRouterLayout();
	
	/**
	 * U
	 * @param routerLayout
	 */
	public void setRouterLayout(OwlcmsRouterLayout routerLayout);
	
	/**
	 * @return
	 */
	public default AbstractLeftAppLayoutBase getAppLayout() {
		return (AbstractLeftAppLayoutBase) getRouterLayout().getAppLayout();
	}

}