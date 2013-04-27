package help;

/**
 * Help for a certain processing step.
 * @author xnikp
 *
 */
public class HelpContent {
	private String title;
	private String content;
	
	public HelpContent() {
		
	}
	
	public HelpContent( String t, String c ) {
		title = t;
		content = c;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean hasContent() {
		return this.content != null;
	}
	
	public boolean hasTitle() {
		return this.title != null;
	}
}
