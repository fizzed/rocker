public class Main {

	static public void main(String[]args){

		String output=views.HelloWorld
				.template("World")
				.render()
				.toString();

	}
}