package neoe.build.tools;


public class Project1 {
	String name;
	Projects prjs;
	public int skipJavac;
	public int skipResource;

	public Project1(Projects prjs) {
		this.prjs=prjs;
	}

	public void setName(String prjName) {
		name = prjName;
	}

}
