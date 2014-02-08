package test;

public class Test_Faintness_trycatch {
  public static void main() {
	  
	  try {
		  int[] x = new int[1];
		  int y = x[1];
		  //System.out.println(x[1]);
		  //return;
      } catch(RuntimeException e) {
	  	  int z = 2;
		  //System.out.println("exception");
	  } finally {
		  int w = 3;
	  }
  }
}


